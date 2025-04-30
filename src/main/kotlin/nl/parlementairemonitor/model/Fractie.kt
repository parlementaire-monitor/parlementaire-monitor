package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer

@Serializable
data class Fractie (
    @SerialName("_id") val id: String,
    val afkorting: String?,
    val naamNl: String?,
    val naamEn: String?,
    val aantalZetels: Int?,
    @Serializable(TimestampSerializer::class) val datumActief: Long?,
    @Serializable(TimestampSerializer::class) val datumInactief: Long?,
    val zetels: List<Zetel>?,
)

