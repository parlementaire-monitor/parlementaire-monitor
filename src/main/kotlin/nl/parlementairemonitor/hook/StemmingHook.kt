package nl.parlementairemonitor.hook

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import jakarta.xml.bind.JAXBElement
import kotlinx.coroutines.flow.toList
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.model.ProcessQueueRequest
import nl.parlementairemonitor.service.AdminService
import nl.parlementairemonitor.task.PostProcessor
import nl.parlementairemonitor.task.PostProcessor.Task
import nl.tweedekamer.xsd.tkdata.v1_0.StemmingType
import org.bson.Document

class StemmingHook : PostProcessor.Hook {

    override suspend fun filter(content: JAXBElement<*>): Task? {
        if (content.declaredType != StemmingType::class.java) {
            return null
        }
        val type = content.value as? StemmingType ?: return null
        return StemmingTask(type)
    }

    val database: MongoDatabase by lazy {
        MongoDb.getDatabase()
    }

    inner class StemmingTask(val type: StemmingType) : Task {
        val collection = database.getCollection<Document>("Besluit")
        override suspend fun process() {
            val besluiten = collection.find(Filters.eq("_id", type.besluit.ref)).toList()
            besluiten.forEach { besluit ->
                besluit.getList("zaak", String::class.java).forEach { zaak ->
                    AdminService.addQueueItems(
                        ProcessQueueRequest(
                            collection = "Document",
                            filter = Document(mapOf("zaak" to zaak.toString())),
                            delay = "1h",
                            dryRun = false,
                        )
                    )
                }
            }
        }
    }

}