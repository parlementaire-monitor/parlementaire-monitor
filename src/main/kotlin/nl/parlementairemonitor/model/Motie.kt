package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer

@Serializable
data class Motie(
    @SerialName("_id") val id: String,
    val titel: String? = null,
    val datum: @Serializable(TimestampSerializer::class) Long? = null,
    val citeertitel: String? = null,
    val volledigeTekst: String? = null,
    val context: List<String>? = null,
    val motie: List<String>? = null,
    val documentactor: List<DocumentActor>? = null,
    val besluit: Besluit? = null,
    val stemming: List<Stemming>? = null,
)

