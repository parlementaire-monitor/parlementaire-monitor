package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer

@Serializable
data class Motie(
    @SerialName("_id") val id: String,
    val titel: String?,
    @Serializable(TimestampSerializer::class) val datum: Long?,
    val citeertitel: String?,
    val volledigeTekst: String?,
    val context: List<String>?,
    val motie: List<String>?,
    val documentactor: List<DocumentActor>?,
    val besluit: Besluit?,
    val stemming: List<Stemming>?,
)

