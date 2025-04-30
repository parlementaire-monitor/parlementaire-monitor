package nl.parlementairemonitor

import io.ktor.server.application.Application
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic

fun Application.configureSecurity() {
    authentication {
        basic(name = "admin-user") {
            realm = "Parlementaire Monitor | Admin"
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}
