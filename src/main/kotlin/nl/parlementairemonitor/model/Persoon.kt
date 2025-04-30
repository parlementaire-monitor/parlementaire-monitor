package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer

@Serializable
data class Persoon(
    @SerialName("_id") val id: String,
    val titels: String?,
    val initialen: String?,
    val tussenvoegsel: String?,
    val achternaam: String?,
    val voornamen: String?,
    val roepnaam: String?,
    val geslacht: String?,
    val functie: String?,
    @Serializable(TimestampSerializer::class) val geboortedatum: Long?,
    val geboorteplaats: String?,
    val geboorteland: String?,
    @Serializable(TimestampSerializer::class) val overlijdensdatum: Long?,
    val overlijdensplaats: String?,
    val woonplaats: String?,
    val land: String?,
    val fractie: String?,
    val zetels: List<Zetel>?
) {
    fun displayName(): String {
        if (tussenvoegsel.isNullOrEmpty()) return "$roepnaam $achternaam"
        return "$roepnaam $tussenvoegsel $achternaam"
    }
}

