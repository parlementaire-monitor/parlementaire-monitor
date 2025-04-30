package nl.parlementairemonitor.model

import kotlinx.serialization.Serializable

@Serializable
data class ProcessQueueResponse(
    val count: Long,
)