package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer

@Serializable
data class Persoon(
    @SerialName("_id") val id: String,
    val titels: String? = null,
    val initialen: String? = null,
    val tussenvoegsel: String? = null,
    val achternaam: String? = null,
    val voornamen: String? = null,
    val roepnaam: String? = null,
    val geslacht: String? = null,
    val functie: String? = null,
    @Serializable(TimestampSerializer::class) val geboortedatum: Long? = null,
    val geboorteplaats: String? = null,
    val geboorteland: String? = null,
    @Serializable(TimestampSerializer::class) val overlijdensdatum: Long? = null,
    val overlijdensplaats: String? = null,
    val woonplaats: String? = null,
    val land: String? = null,
    val fractie: String? = null,
    val zetels: List<Zetel>? = null,
) {
    fun displayName(): String {
        if (tussenvoegsel.isNullOrEmpty()) return "$roepnaam $achternaam"
        return "$roepnaam $tussenvoegsel $achternaam"
    }
}

