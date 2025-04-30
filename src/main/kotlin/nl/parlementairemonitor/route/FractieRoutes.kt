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
import nl.parlementairemonitor.model.Fractie
import nl.parlementairemonitor.model.Zetel
import nl.parlementairemonitor.service.FractieService
import nl.parlementairemonitor.util.FileUtil
import nl.parlementairemonitor.util.FilterUtil

val fractieRoutes: RouterGroup = {
    
    route("/fracties", {
        tags = listOf("Fractie")
        description = "APIs for retrieving fractie information"
    }) {
        
        get({
            summary = "Get all fracties"
            description = "Returns a list of all parliamentary fracties"
            request {
                FilterUtil.queryParams(this)
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                    body<List<Fractie>> {
                        required = true
                        description = "the list of fracties"
                    }
                }
            }
        }) {
            call.respond(FractieService.list(Zetel.filter(call)))
        }
        
        get("/{id}", {
            summary = "Get fractie by ID"
            description = "Returns detailed information about a specific fractie including seat information"
            request {
                pathParameter<String>("id") {
                    description = "id of the fractie"
                }
                FilterUtil.queryParams(this)
            }
            response {
                HttpStatusCode.OK to {
                    description = "OK"
                    body<Fractie> {
                        required = true
                        description = "the fractie"
                    }
                }
                HttpStatusCode.NotFound to {
                    description = "Not Found"
                }
            }
        }) {
            call.respond(FractieService.get(call.id(), Zetel.filter(call)))
        }
        
        get("/{id}/image", {
            summary = "Get fractie logo"
            description = "Returns an image of the fractie logo"
            request {
                pathParameter<String>("id") {
                    description = "id of the fractie"
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
            val doc = FractieService.image(call.id())
            val contentType = (doc["contentType"] as? String) ?: "image"
            val file = FileUtil.getResourceFile(doc)
            if (! file.exists()) throw NotFoundException()
            call.respondBytes(file.readBytes(), ContentType.parse(contentType), HttpStatusCode.OK)
        }
    }
}
