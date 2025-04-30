package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentActor(
    @SerialName("_id") val id: String,
    val actorNaam: String?,
    val actorFractie: String?,
    val functie: String?,
    val relatie: String?,
)