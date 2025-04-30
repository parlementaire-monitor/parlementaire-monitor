package nl.parlementairemonitor.hook

import com.mongodb.client.model.Aggregates.lookup
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Aggregates.unwind
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Variable
import jakarta.xml.bind.JAXBElement
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.task.PostProcessor
import nl.parlementairemonitor.util.TextUtil
import nl.tweedekamer.xsd.tkdata.v1_0.DocumentType
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.ooxml.extractor.POIXMLExtractorFactory
import org.bson.Document
import java.io.File
import java.io.FileInputStream
import kotlin.collections.set

class MotieHook : PostProcessor.Hook {

    override suspend fun filter(content: JAXBElement<*>): PostProcessor.Task? {
        if (content.declaredType != DocumentType::class.java) {
            return null
        }
        val document = content.value as? DocumentType ?: return null
        val soort = document.soort?.value?.value ?: return null
        if (soort != "Motie" && soort != "Motie (gewijzigd/nader)") {
            return null
        }
        return MotieTask(document)
    }

    class MotieTask(private val document: DocumentType) : DownloadTask(document) {

        private val database by lazy {
            MongoDb.getDatabase()
        }

        override suspend fun postDownload() {
            val motie = when (document.contentType) {
                "application/pdf" -> parsePdf(file())
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> parseDocx(file())
                "application/msword" -> parseDoc(file())
                else -> throw Exception("Unexpected content: ${document.contentType}")
            }
            val collection = database.getCollection<Document>("Document")
            val doc = collection.aggregate<Document>(
                listOf(
                    match(Filters.eq("_id", document.id)),
                    unwind("\$zaak"),
                    lookup(
                        "Besluit",
                        listOf(Variable("zaak", "\$zaak")),
                        listOf(match(Filters.expr(
                            Document("\$and", listOf(
                                Document("\$in", listOf("\$\$zaak", "\$zaak")),
                                Document("\$ne", listOf("\$stemmingsSoort", null)),
                            )),
                        ))),
                        "besluit"
                    ),
                    unwind("\$besluit"),
                    lookup("Stemming", "besluit._id", "besluit", "stemming"),
                    lookup("DocumentActor", "_id", "document", "documentactor"),
                )
            ).toList().firstOrNull()

            val bsonMotie = Document.parse(Json.encodeToString(Motie.serializer(), motie))
            val zoekTekst = TextUtil.makeSearchableText(
                motie.context.joinToString(" ") + " " + motie.motie.joinToString(" ")
            )

            val motieDocument = Document()
            motieDocument["_id"] = document.id
            motieDocument.putAll(bsonMotie)
            motieDocument["zoekTekst"] = zoekTekst
            doc?.let {
                motieDocument.putAll(it.filterValues { v -> v != null })
            }

            database.getCollection<Document>("Motie").replaceOne(
                Filters.eq("_id", document.id),
                motieDocument,
                ReplaceOptions().upsert(true)
            )
        }
    }

    companion object {

        private val CONTEXT_PREFIX: (String) -> Boolean = { s ->
            listOf("constaterende", "overwegende", "van mening")
                .any { s.startsWith(it, ignoreCase = true) }
        }
        private val MOTIE_PREFIX: (String) -> Boolean = { s ->
            listOf("verzoekt", "spreekt", "besluit", "roept", "zegt")
                .any { s.startsWith(it, ignoreCase = true) }
        }

        fun parsePdf(file: File): Motie {
            PDDocument.load(file).use { document ->
                val pdfStripper = PDFTextStripper()
                val fullContent = pdfStripper.getText(document)
                val allLines = fullContent.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toList()

                val processed = mutableListOf<StringBuilder>()
                var processing = false

                for (line in allLines) {
                    when {
                        line.startsWith("gehoord de beraadslaging", ignoreCase = true) -> {
                            processing = true
                            continue // Skip this line
                        }
                        line.startsWith("en gaat over tot de orde van de dag", ignoreCase = true) -> {
                            break // Stop processing
                        }
                        processing -> {
                            if (CONTEXT_PREFIX(line) || MOTIE_PREFIX(line)) {
                                // Start a new paragraph
                                processed.add(StringBuilder(line))
                            } else {
                                // Continue last paragraph
                                if (processed.isNotEmpty()) {
                                    processed.last().append(' ').append(line)
                                } else {
                                    processed.add(StringBuilder(line))
                                }
                            }
                        }
                    }
                }
                val context = processed.map { it.toString() }.filter(CONTEXT_PREFIX)
                val motie = processed.map { it.toString() }.filter(MOTIE_PREFIX)

                return Motie(allLines.joinToString("\n"), context, motie)
            }
        }

        fun parseDocx(file: File): Motie {
            FileInputStream(file).use { fis ->
                val doc = POIXMLExtractorFactory().create(fis, null)
                val allLines = doc.text
                    .lines()
                    .filter { it.trim().isNotEmpty() }
                    .map { it.replace("\t\\s*\t".toRegex(),"\n").lines() }
                    .flatten()
                    .map { it.trim() }

                val fullContent = allLines.joinToString("\n")
                val context = allLines.filter(CONTEXT_PREFIX)
                val motie = allLines.filter(MOTIE_PREFIX)

                return Motie(fullContent, context, motie)
            }
        }

        fun parseDoc(file: File): Motie {
            FileInputStream(file).use { fis ->
                val doc = HWPFDocument(fis)
                val extractor = WordExtractor(doc)

                val allLines = extractor.text
                    .lines()
                    .filter { it.trim().isNotEmpty() }
                    .map { it.replace("\t\\s*\t".toRegex(),"\n").lines() }
                    .flatten()
                    .map { it.trim() }

                val fullContent = allLines.joinToString("\n")
                val context = allLines.filter(CONTEXT_PREFIX)
                val motie = allLines.filter(MOTIE_PREFIX)

                return Motie(fullContent, context, motie)
            }
        }
    }

    @Serializable
    data class Motie(
        val volledigeTekst: String,
        val context: List<String>,
        val motie: List<String>
    )

}
