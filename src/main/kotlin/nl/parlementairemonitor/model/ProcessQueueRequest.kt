package nl.parlementairemonitor.model

import kotlinx.serialization.Serializable
import nl.parlementairemonitor.BsonSerializer
import org.bson.Document

@Serializable
data class ProcessQueueRequest(
    val collection: String,
    val filter: @Serializable(BsonSerializer::class) Document,
    val delay: String,
    val dryRun: Boolean = false,
)

