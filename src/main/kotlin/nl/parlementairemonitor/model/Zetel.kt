package nl.parlementairemonitor.model

import io.ktor.server.routing.RoutingCall
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.parlementairemonitor.TimestampSerializer
import nl.parlementairemonitor.util.Filter
import nl.parlementairemonitor.util.FilterUtil

@Serializable
data class Zetel (
    @SerialName("_id") val id: String,
    val functie: String? = null,
    val van: @Serializable(TimestampSerializer::class) Long? = null,
    val totEnMet: @Serializable(TimestampSerializer::class) Long? = null,
    val persoon: Persoon? = null,
    val fractie: Fractie? = null,
) {
    companion object {
        fun filter(call: RoutingCall): Filter<Zetel> {
            val (all, at) = FilterUtil.fromParams(call)
            return {
                all || ((it.van ?: Long.MAX_VALUE) <= at && at <= (it.totEnMet ?: Long.MAX_VALUE))
            }
        }
    }
}