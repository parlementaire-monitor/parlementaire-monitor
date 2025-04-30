package nl.parlementairemonitor.model

import kotlinx.serialization.Serializable

@Serializable
data class Paginated<T>(
    val data: List<T>,
    val count: Int,
    val page: Int,
    val size: Int,
)
