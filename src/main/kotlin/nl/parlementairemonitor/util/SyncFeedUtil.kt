package nl.parlementairemonitor.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.Unmarshaller
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.task.PostProcessor
import nl.tweedekamer.xsd.tkdata.v1_0.EntiteitType
import org.slf4j.LoggerFactory
import java.io.InputStream

object SyncFeedUtil {

    const val SYNC_FEED_BASE_URL = "https://gegevensmagazijn.tweedekamer.nl/SyncFeed/2.0"

    private val log = LoggerFactory.getLogger(this::class.java)

    private val context: JAXBContext = JAXBContext.newInstance("nl.tweedekamer.xsd.tkdata.v1_0")
    private val unmarshaller: Unmarshaller = context.createUnmarshaller()
    private val objectMapper = ObjectMapper().registerModule(JaxbAnnotationModule())
    private val database by lazy {
        MongoDb.getDatabase()
    }

    suspend fun processXml(inputStream: InputStream): JAXBElement<*> {

        val content = unmarshalJAXBElement(inputStream)

        val collectionName = content.declaredType.simpleName.dropLast(4)
        val collection = database.getCollection<Map<String, Any?>>(collectionName)

        if (content.value is EntiteitType) {
            val entiteitType = content.value as EntiteitType
            if (entiteitType.isVerwijderd) {
                collection.deleteOne(Filters.eq("_id", entiteitType.id))
                return content
            }
        }

        val map = convertToMap(content)
        collection.replaceOne(
            Filters.eq("_id", map["_id"]),
            map,
            ReplaceOptions().upsert(true)
        )
        PostProcessor.process(content)

        return content
    }

    fun unmarshalJAXBElement(inputStream: InputStream): JAXBElement<*> {
        try {
            synchronized(unmarshaller) {
                return unmarshaller.unmarshal(inputStream) as JAXBElement<*>
            }
        } catch (e: Exception) {
            log.error("Unable to parse: ${e.message}")
            throw e
        }
    }

    fun convertToMap(content: JAXBElement<*>): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        for ((k, v) in objectMapper.convertValue(content.value, Map::class.java)) {
            val key = if (k == "id") "_id" else k.toString()
            val value = extractRefOrValue(v)
            map[key] = value
        }
        return map
    }

    private fun extractRefOrValue(value: Any?): Any? {
        return when (value) {
            is Map<*, *> -> value["ref"] ?: extractRefOrValue(value["value"])
            is List<*> -> value.map { extractRefOrValue(it) }
            else -> value
        }
    }

}