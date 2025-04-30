package nl.parlementairemonitor.route

import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.response.respond
import nl.parlementairemonitor.RouterGroup
import nl.parlementairemonitor.id
import nl.parlementairemonitor.model.Motie
import nl.parlementairemonitor.model.Paginated
import nl.parlementairemonitor.service.MotieService
import nl.parlementairemonitor.util.Pagination

val motieRoutes: RouterGroup = {

    route("/moties", {
        tags = listOf("Motie")
        description = "APIs for retrieving motie information"
    }) {

        get({
            summary = "Search moties"
            description = "Returns a list of moties that match the search query"
            request {
                queryParameter<String>("q") {
                    required = true
                    description = "keywords to search for"
                }
                Pagination.queryParams(this)
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                    body<Paginated<Motie>> {
                        required = true
                        description = "the paginated moties"
                    }
                }
            }
        }) {
            val q = call.queryParameters["q"]
                ?: throw MissingRequestParameterException("Query parameter 'q' is not set")
            call.respond(MotieService.search(q, Pagination.fromParams(call)))
        }

        get("/{id}", {
            summary = "Get motie by ID"
            description = "Returns detailed information about a specific motie including voting information"
            request {
                pathParameter<String>("id") {
                    required = true
                    description = "id of the motie"
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                    body<Motie> {
                        required = true
                        description = "the moties"
                    }
                }
                HttpStatusCode.NotFound to {
                    description = "Not Found"
                }
            }
        }) {
            call.respond<Motie>(MotieService.get(call.id()))
        }
    }
}
