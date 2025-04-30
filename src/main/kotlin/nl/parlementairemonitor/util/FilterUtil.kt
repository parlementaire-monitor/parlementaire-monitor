package nl.parlementairemonitor.util

import io.github.smiley4.ktoropenapi.config.RequestConfig
import io.ktor.server.routing.RoutingCall
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

typealias Filter<T> = (T) -> Boolean

object FilterUtil {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun fromParams(call: RoutingCall): Pair<Boolean, Long> {
        val atParam = call.queryParameters["at"]
        val all = atParam?.lowercase() == "all"
        val at = atParam?.let {
            try {
                LocalDate.parse(it, formatter)
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli()
            } catch (e: Exception) {
                Instant.now().toEpochMilli()
            }
        } ?: Instant.now().toEpochMilli()
        return Pair(all, at)
    }

    val queryParams: RequestConfig.() -> Unit = {
        queryParameter<String>("at") {
            description = "Date for which to get the result (yyyy-MM-dd) or `all` for all data"
            required = false
        }
    }

}