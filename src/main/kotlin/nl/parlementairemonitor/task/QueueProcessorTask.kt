package nl.parlementairemonitor.task

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.util.SyncFeedUtil
import org.slf4j.LoggerFactory
import java.time.Instant

class QueueProcessorTask private constructor() {

    companion object {
        val INSTANCE: QueueProcessorTask by lazy {
            QueueProcessorTask()
        }
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private val collection by lazy {
        MongoDb.getDatabase().getCollection("processqueue", QueueItem::class.java)
    }
    private val httpClient = HttpClient(Java)

    private var running: Boolean = false
    private var keepGoing: Boolean = true

    suspend fun run() {
        if (running) return
        synchronized(this) {
            if (running) return
            running = true
        }
        try {
            processQueue()
        } catch (e: Exception) {
            log.error("Error during task", e)
        } finally {
            running = false
        }
    }

    private suspend fun processQueue() {

        var queueItems = getQueueItems()

        while (queueItems.isNotEmpty() && keepGoing) {
            log.info("Processing ${queueItems.size} queueItems")

            queueItems.forEach { item ->
                val response = httpClient.request {
                    url("${SyncFeedUtil.SYNC_FEED_BASE_URL}/Entiteiten/${item.id}")
                    accept(ContentType.Application.Xml)
                }

                SyncFeedUtil.processXml(response.bodyAsChannel().toInputStream())

            }

            collection.deleteMany(
                Filters.`in`("_id", queueItems.map { it.id }),
            )

            queueItems = getQueueItems()
        }
    }

    private suspend fun getQueueItems(): List<QueueItem> {
        return collection
            .find<QueueItem>(Filters.lt("processAfter", Instant.now().toEpochMilli()))
            .sort(Sorts.ascending("queueTime"))
            .limit(100)
            .toList()
    }

    fun stop() {
        log.info("Stopping SyncFeedTask")
        keepGoing = false
    }

    @Serializable
    private data class QueueItem(
        @SerialName("_id") val id: String,
        val queueTime: Long,
        val processAfter: Long,
    )

}
