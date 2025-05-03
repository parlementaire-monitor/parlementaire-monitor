package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer

@Serializable
data class Fractie (
    @SerialName("_id") val id: String,
    val afkorting: String? = null,
    val naamNl: String? = null,
    val naamEn: String? = null,
    val aantalZetels: Int? = null,
    val datumActief: @Serializable(TimestampSerializer::class) Long? = null,
    val datumInactief: @Serializable(TimestampSerializer::class) Long? = null,
    val zetels: List<Zetel>? = null,
)

