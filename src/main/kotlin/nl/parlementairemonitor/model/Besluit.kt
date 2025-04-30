package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Besluit(
    @SerialName("_id") val id: String,
    val stemmingsSoort: String?,
    val besluitSoort: String?,
    val besluitTekst: String?,
    val status: String?,
)