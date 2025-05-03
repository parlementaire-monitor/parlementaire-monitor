package nl.parlementairemonitor.service

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.MergeOptions
import com.mongodb.client.model.MergeOptions.WhenMatched.KEEP_EXISTING
import com.mongodb.client.model.MergeOptions.WhenNotMatched.INSERT
import com.mongodb.client.model.Projections
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.model.ProcessQueueRequest
import org.bson.Document
import java.time.Instant
import kotlin.time.Duration

object AdminService {
    private val database by lazy {
        MongoDb.getDatabase()
    }

    suspend fun addQueueItems(request: ProcessQueueRequest): Int {
        val collection = database.getCollection<Document>(request.collection)
        val filter = request.filter.toBsonDocument()
        val queueTime = Instant.now().toEpochMilli()
        val processAfter = queueTime + Duration.parse(request.delay).inWholeMilliseconds
        if (request.dryRun) {
            return collection.find(filter).count()
        }
        val insert = collection.aggregate(
            listOf(
                Aggregates.match(filter),
                Aggregates.project(
                    Projections.fields(
                        Document("_id", 1),
                        Document("queueTime", Document("\$literal", queueTime)),
                        Document("processAfter", Document("\$literal", processAfter)),
                    )
                ),
                Aggregates.merge(
                    "processqueue", MergeOptions()
                        .whenMatched(KEEP_EXISTING)
                        .whenNotMatched(INSERT)
                ),
            )
        ).toList()
        return insert.size
    }

    suspend fun clearQueue(): Long {
        val collection = database.getCollection<Document>("processqueue")
        return collection.deleteMany(Filters.empty()).deletedCount
    }

}