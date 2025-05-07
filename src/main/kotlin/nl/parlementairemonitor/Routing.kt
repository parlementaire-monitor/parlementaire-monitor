package nl.parlementairemonitor

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import nl.parlementairemonitor.cache.FractieCache
import nl.parlementairemonitor.cache.PersoonCache
import nl.parlementairemonitor.route.adminRoutes
import nl.parlementairemonitor.route.commissieRoutes
import nl.parlementairemonitor.route.fractieRoutes
import nl.parlementairemonitor.route.motieRoutes
import nl.parlementairemonitor.route.persoonRoutes

fun Application.configureRouting() {
    routing {
        fractieRoutes()
        persoonRoutes()
        commissieRoutes()
        motieRoutes()
        adminRoutes()

        get("/healthz") {
            FractieCache.fractieMap
            PersoonCache.persoonMap
            call.respondText("OK", contentType = ContentType.Text.Plain, status = HttpStatusCode.OK)
        }
    }
}

typealias RouterGroup = Route.() -> Unit

fun RoutingCall.id(): String {
    return pathParameters["id"] ?: throw BadRequestException("Missing ID")
}