package nl.parlementairemonitor.route

import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import nl.parlementairemonitor.RouterGroup
import nl.parlementairemonitor.id
import nl.parlementairemonitor.model.Commissie
import nl.parlementairemonitor.model.CommissiePersoon
import nl.parlementairemonitor.service.CommissieService
import nl.parlementairemonitor.util.FilterUtil

val commissieRoutes: RouterGroup = {

    route("/commissies", {
        tags = listOf("Commissie")
        description = "APIs for retrieving commissie information"
    }) {

        get({
            summary = "Get all commissies"
            description = "Returns a list of all parliamentary commissies without detailed seat information"
            request {
                FilterUtil.queryParams(this)
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                    body<List<Commissie>> {
                        description = "List of commissies"
                        required = true
                    }
                }
            }
        }) {
            call.respond<List<Commissie>>(CommissieService.list(Commissie.filter(call)))
        }

        get("{id}", {
            summary = "Get commissie by ID"
            description =
                "Returns detailed information about a specific parliamentary commissie including seat information"
            request {
                pathParameter<String>("id") {
                    description = "Commissie ID"
                    required = true
                }
                FilterUtil.queryParams(this)
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                    body<Commissie> {
                        description = "Commission details"
                        required = true
                    }
                }
                HttpStatusCode.NotFound to {
                    description = "Not Found"
                }
            }
        }) {
            call.respond<Commissie>(
                CommissieService.get(call.id(), CommissiePersoon.filter(call))
            )
        }
    }
}
