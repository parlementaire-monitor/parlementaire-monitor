package nl.parlementairemonitor

import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()
    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureRouting()
    configureAdministration()
    configureMonitoring()
}
