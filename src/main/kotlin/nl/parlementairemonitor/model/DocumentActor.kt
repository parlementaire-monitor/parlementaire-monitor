package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentActor(
    @SerialName("_id") val id: String,
    val actorNaam: String? = null,
    val actorFractie: String? = null,
    val functie: String? = null,
    val relatie: String? = null,
)