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
    val functie: String?,
    @Serializable(TimestampSerializer::class) val van: Long?,
    @Serializable(TimestampSerializer::class) val totEnMet: Long?,
    val persoon: Persoon?,
    val fractie: Fractie?,
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