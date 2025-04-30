package nl.parlementairemonitor

import io.ktor.server.application.Application
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.routing
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
    }
}

typealias RouterGroup = Route.() -> Unit

fun RoutingCall.id(): String {
    return pathParameters["id"] ?: throw BadRequestException("Missing ID")
}