package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stemming(
    @SerialName("_id") val id: String,
    val soort: String? = null,
    val actorNaam: String? = null,
    val actorFractie: String? = null,
    val fractieGrootte: Int? = null,
)