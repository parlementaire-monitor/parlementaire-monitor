package nl.parlementairemonitor.task

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.util.SyncFeedUtil
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

class SyncFeedTask private constructor() {

    companion object {
        val INSTANCE: SyncFeedTask = SyncFeedTask()
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private val database by lazy {
        MongoDb.getDatabase()
    }
    private var running: Boolean = false
    private var keepGoing: Boolean = true

    suspend fun run(feed: String = "next") {
        if (running) return
        synchronized(this) {
            if (running) return
            running = true
        }
        try {
            updateSyncFeed(feed)
        } catch (e: Exception) {
            log.error("Failed to sync feed", e)
        } finally {
            running = false
        }
    }

    private suspend fun updateSyncFeed(feed: String) {
        var feedUrl = database.getCollection("syncfeed", SyncFeed::class.java)
            .find(Filters.eq("_id", feed))
            .firstOrNull()?.url ?: "${SyncFeedUtil.SYNC_FEED_BASE_URL}/Feed"

        var hasNext = true

        log.info("Starting at $feedUrl")
        do {
            val httpClient = HttpClient(Java)
            val response = httpClient.request {
                url(feedUrl)
                accept(ContentType.Application.Xml)
            }
            XmlReader(response.bodyAsChannel().toInputStream()).use { reader ->
                var nextUrl = feedUrl
                SyndFeedInput().build(reader).entries.forEach { entry ->
                    entry.contents.forEach {

                        SyncFeedUtil.processXml(it.value.byteInputStream(StandardCharsets.UTF_8))

                    }
                    entry.links.first { link -> link.rel == "next" }.let { link ->
                        nextUrl = link.href
                    }
                }
                if (feedUrl == nextUrl) {
                    hasNext = false
                } else {
                    database.getCollection("syncfeed", SyncFeed::class.java).replaceOne(
                        Filters.eq("_id", feed),
                        SyncFeed(feed, nextUrl),
                        ReplaceOptions().upsert(true)
                    )
                    feedUrl = nextUrl
                }
            }
        } while (hasNext && keepGoing)
    }

    fun stop() {
        log.info("Stopping SyncFeedTask")
        keepGoing = false
    }

    @Serializable
    private data class SyncFeed(
        @SerialName("_id") val id: String,
        val url: String,
    )
}
