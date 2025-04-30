package nl.parlementairemonitor.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ProcessQueueRequest(
    val collection: String,
    val filter: JsonObject,
    val delay: String,
    val dryRun: Boolean = false,
)