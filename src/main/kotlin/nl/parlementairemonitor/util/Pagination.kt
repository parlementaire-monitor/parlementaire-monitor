package nl.parlementairemonitor.util

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Sorts
import io.github.smiley4.ktoropenapi.config.RequestConfig
import io.ktor.server.routing.RoutingCall
import nl.parlementairemonitor.util.Pagination.SortDirection.ASC
import nl.parlementairemonitor.util.Pagination.SortDirection.DESC
import org.bson.conversions.Bson

data class Pagination(
    val sort: String?,
    val direction: SortDirection?,
    val page: Int = 1,
    val size: Int = 10,
) {
    enum class SortDirection {
        ASC,
        DESC,
    }

    fun sort(defaultSort: String, defaultDirection: SortDirection): Bson {
        return when (direction ?: defaultDirection) {
            ASC -> Sorts.ascending(sort ?: defaultSort)
            DESC -> Sorts.descending(sort ?: defaultSort)
        }
    }

    fun facet(): List<Bson> {
        return listOf(
            Aggregates.skip((page - 1) * size),
            Aggregates.limit(size)
        )
    }

    companion object {
        fun fromParams(call: RoutingCall): Pagination {
            return Pagination(
                sort = call.request.queryParameters["sort"],
                direction = when (call.request.queryParameters["direction"]?.lowercase()) {
                    "asc" -> ASC
                    "desc" -> DESC
                    else -> null
                },
                page = call.request.queryParameters["page"]?.toInt() ?: 1,
                size = call.request.queryParameters["size"]?.toInt() ?: 10,
            )
        }

        val queryParams: RequestConfig.() -> Unit = {
            queryParameter<String>("sort") {
                required = false
                description = "field on which to sort the results"
            }
            queryParameter<SortDirection>("direction") {
                required = false
                description = "field on which to sort the results"
            }
            queryParameter<Int>("page") {
                required = false
                description = "page of the paginated results to show"
            }
            queryParameter<Int>("size") {
                required = false
                description = "number of results to show per page"
                location
            }
        }
    }
}