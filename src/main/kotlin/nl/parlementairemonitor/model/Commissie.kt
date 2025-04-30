package nl.parlementairemonitor.model

import io.ktor.server.routing.RoutingCall
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer
import nl.parlementairemonitor.util.Filter
import nl.parlementairemonitor.util.FilterUtil

@Serializable
data class Commissie(
    @SerialName("_id") val id: String,
    val soort: String?,
    val afkorting: String?,
    val naamNl: String?,
    val naamEn: String?,
    val naamWebNl: String?,
    val naamWebEn: String?,
    val inhoudsopgave: String?,
    @Serializable(TimestampSerializer::class) val datumActief: Long?,
    @Serializable(TimestampSerializer::class) val datumInactief: Long?,
    val zetels: List<CommissieZetel>?,
) {
    companion object {
        fun filter(call: RoutingCall): Filter<Commissie> {
            val (all, at) = FilterUtil.fromParams(call)
            return {
                all
                || (it.datumActief == null && it.datumInactief == null)
                || ((it.datumActief ?: Long.MAX_VALUE) <= at && at <= (it.datumInactief ?: Long.MAX_VALUE))
            }
        }
    }
}

