package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Besluit(
    @SerialName("_id") val id: String,
    val stemmingsSoort: String? = null,
    val besluitSoort: String? = null,
    val besluitTekst: String? = null,
    val status: String? = null,
)