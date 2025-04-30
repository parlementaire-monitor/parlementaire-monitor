package nl.parlementairemonitor.route

import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import nl.parlementairemonitor.RouterGroup
import nl.parlementairemonitor.id
import nl.parlementairemonitor.model.Persoon
import nl.parlementairemonitor.model.Zetel
import nl.parlementairemonitor.service.PersoonService
import nl.parlementairemonitor.util.FileUtil
import nl.parlementairemonitor.util.FilterUtil

val persoonRoutes: RouterGroup = {

    route("/personen", {
        tags = listOf("Persoon")
        description = "APIs for retrieving persoon information"
    }) {

        get({
            summary = "Get all personen"
            description = "Returns a list of all Member of Parliament personen"
            request {
                FilterUtil.queryParams(this)
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                    body<List<Persoon>> {
                        required = true
                        description = "the list of personen"
                    }
                }
            }
        }) {
            call.respond(PersoonService.list(Zetel.filter(call)))
        }

        get("/{id}", {
            summary = "Get persoon by ID"
            description = "Returns detailed information about a specific persoon including seat information"
            request {
                pathParameter<String>("id") {
                    description = "id of the persoon"
                }
                FilterUtil.queryParams(this)
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                    body<Persoon> {
                        required = true
                        description = "the persoon"
                    }
                }
                HttpStatusCode.NotFound to {
                    description = "Not Found"
                }
            }
        }) {
            call.respond(PersoonService.get(call.id(), Zetel.filter(call)))
        }

        get("/{id}/image", {
            summary = "Get persoon photo"
            description = "Returns an photo of the persoon"
            request {
                pathParameter<String>("id") {
                    description = "id of the persoon"
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                }
                HttpStatusCode.NotFound to {
                    description = "Not Found"
                }
            }
        }) {
            val doc = PersoonService.image(call.id())
            val contentType = (doc["contentType"] as? String) ?: "image"
            val file = FileUtil.getResourceFile(doc)
            if (!file.exists()) throw NotFoundException()
            call.respondBytes(file.readBytes(), ContentType.parse(contentType), HttpStatusCode.OK)
        }
    }
}