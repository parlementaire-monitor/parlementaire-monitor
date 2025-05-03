package nl.parlementairemonitor.model

import io.ktor.server.routing.RoutingCall
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer
import nl.parlementairemonitor.util.Filter
import nl.parlementairemonitor.util.FilterUtil

@Serializable
data class CommissiePersoon(
    @SerialName("_id") val id: String,
    val persoon: String? = null,
    val naam: String? = null,
    val functie: String? = null,
    val van: @Serializable(TimestampSerializer::class) Long? = null,
    val totEnMet: @Serializable(TimestampSerializer::class) Long? = null,
) {
    companion object {
        fun filter(call: RoutingCall): Filter<CommissiePersoon> {
            val (all, at) = FilterUtil.fromParams(call)
            return { zetel ->
                all || ((zetel.van ?: Long.MAX_VALUE) <= at && at <= (zetel.totEnMet ?: Long.MAX_VALUE))
            }
        }
    }
}