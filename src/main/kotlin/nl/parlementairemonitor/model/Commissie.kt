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
    val soort: String? = null,
    val afkorting: String? = null,
    val naamNl: String? = null,
    val naamEn: String? = null,
    val naamWebNl: String? = null,
    val naamWebEn: String? = null,
    val inhoudsopgave: String? = null,
    val datumActief: @Serializable(TimestampSerializer::class) Long? = null,
    val datumInactief: @Serializable(TimestampSerializer::class) Long? = null,
    val zetels: List<CommissieZetel>? = null,
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

