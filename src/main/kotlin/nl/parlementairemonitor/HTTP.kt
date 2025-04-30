package nl.parlementairemonitor

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureHTTP() {
    install(Compression)
    install(ForwardedHeaders)
    install(XForwardedHeaders)
    install(OpenApi) {
        server {
            url = System.getenv("SERVER_URL") ?: "https://parlementaire-monitor.nl"
        }
        pathFilter = { _, url ->
            when (url[0]) {
                "docs" -> false
                "metrics" -> false
                else -> true
            }
        }
        schemas {
            generator = SchemaGenerator.kotlinx {
                customAnalyzer({ it.serialName == "TimestampSerializer" }) { _ ->
                    TypeData(
                        id = TypeId.create(),
                        identifyingName = TypeName("kotlin.String", "String"),
                        descriptiveName = TypeName("kotlinx.LocalDate", "Date"),
                        annotations = mutableListOf(
                            AnnotationData(
                                name = Type::class.qualifiedName!!,
                                values = mutableMapOf(
                                    "type" to "string"
                                )
                            )
                        )
                    )
                }
            }
        }
    }
    routing {
        route("/docs") {
            route("api.json") {
                openApi()
            }
            get("swagger.css") {
                val css = """
                    #swagger-ui {
                      background-color: #efefef;
                    }
                    .swagger-container {
                      max-width: 720px;
                      margin: 0 auto;
                      background-color: #ffffff;
                    }
                """.trimIndent()
                call.respondText(css, ContentType.Text.CSS)
            }
            swaggerUI("", "api.json", "") {
                customStyle("/docs/swagger.css")
            }
        }
    }

}
