package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stemming(
    @SerialName("_id") val id: String,
    val soort: String?,
    val actorNaam: String?,
    val actorFractie: String?,
    val fractieGrootte: Int?,
)