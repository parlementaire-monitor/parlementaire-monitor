package nl.parlementairemonitor.route

import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import nl.parlementairemonitor.RouterGroup
import nl.parlementairemonitor.model.ProcessQueueRequest
import nl.parlementairemonitor.model.ProcessQueueResponse
import nl.parlementairemonitor.service.AdminService
import org.bson.Document

val adminRoutes: RouterGroup = {

    authenticate("admin-user") {
        route("/admin", {
            tags = listOf("Admin")
            description = "APIs for admin operations"
        }) {

            route("/queue") {

                post({
                    summary = "Queue items"
                    description = "add items to process queue"
                    request {
                        body<ProcessQueueRequest> {
                            description = "Items to be added"
                            required = true
                            example("Add Documents with soort Motie") {
                                value = ProcessQueueRequest(
                                    collection = "Document",
                                    filter = Document(
                                        mapOf(
                                            "soort" to mapOf("\$eq" to "Motie"),
                                        )
                                    ),
                                    delay = "15m",
                                    dryRun = true,
                                )
                            }
                        }
                    }
                    response {
                        HttpStatusCode.OK to {
                            description = "OK"
                            body<ProcessQueueResponse> {
                                description = "number of items"
                                required = true
                                example("Count") {
                                    value = ProcessQueueResponse(9876)
                                }
                            }
                        }
                    }
                }) {
                    val request = call.receive<ProcessQueueRequest>()
                    val response = ProcessQueueResponse(AdminService.addQueueItems(request).toLong())
                    call.respond<ProcessQueueResponse>(response)
                }

                delete({
                    summary = "Clear process queue"
                    description = "deletes all process queue items"
                    response {
                        HttpStatusCode.OK to {
                            description = "OK"
                            body<ProcessQueueResponse> {
                                required = true
                                description = "number of deleted items"
                                example("Count") {
                                    value = ProcessQueueResponse(9876)
                                }
                            }
                        }
                    }
                }) {
                    call.respond<ProcessQueueResponse>(ProcessQueueResponse(AdminService.clearQueue()))
                }
            }
        }
    }

}
