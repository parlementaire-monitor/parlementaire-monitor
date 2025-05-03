package nl.parlementairemonitor.route

import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.response.respond
import nl.parlementairemonitor.RouterGroup
import nl.parlementairemonitor.id
import nl.parlementairemonitor.model.Besluit
import nl.parlementairemonitor.model.DocumentActor
import nl.parlementairemonitor.model.Motie
import nl.parlementairemonitor.model.Paginated
import nl.parlementairemonitor.model.Stemming
import nl.parlementairemonitor.service.MotieService
import nl.parlementairemonitor.util.Pagination
import java.time.Instant

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
                        example("One result") {
                            value = Paginated(
                                data = listOf(
                                    Motie(
                                        id = "77e92a0c-915b-4dcd-9fcd-694c45f0ddf4",
                                        titel = "Raad Algemene Zaken",
                                        datum = Instant.now().toEpochMilli(),
                                        volledigeTekst = "Lorem ipsum dolor sit amet",
                                        context = listOf("constaterende dit ...", "overwegende dat ..."),
                                        motie = listOf("verzoekt de regering ..."),
                                        besluit = Besluit(
                                            id = "ef6964b4-df43-4955-866a-8c0490f05c7f",
                                            stemmingsSoort = "Met handopsteken",
                                            besluitSoort = "Stemmen - verworpen",
                                            besluitTekst = "Verworpen."
                                        ),
                                    ),
                                ),
                                count = 1,
                                page = 1,
                                size = 10,
                            )
                        }
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
                        example("Single result") {
                            value = Motie(
                                id = "77e92a0c-915b-4dcd-9fcd-694c45f0ddf4",
                                titel = "Raad Algemene Zaken",
                                datum = Instant.now().toEpochMilli(),
                                volledigeTekst = "Lorem ipsum dolor sit amet",
                                context = listOf("constaterende dit ...", "overwegende dat ..."),
                                motie = listOf("verzoekt de regering ..."),
                                besluit = Besluit(
                                    id = "ef6964b4-df43-4955-866a-8c0490f05c7f",
                                    stemmingsSoort = "Met handopsteken",
                                    besluitSoort = "Stemmen - verworpen",
                                    besluitTekst = "Verworpen."
                                ),
                                documentactor = listOf(
                                    DocumentActor(
                                        id = "99da0926-7c13-4bb3-801d-08f7e571d3a8",
                                        actorNaam = "L.A.J.M. Dassen",
                                        actorFractie = "Volt",
                                        functie = "Tweede Kamerlid",
                                        relatie = "Eerste ondertekenaar",
                                    ),
                                ),
                                stemming = listOf(
                                    Stemming(
                                        id = "3340c623-47e9-493d-ae7c-1216ba186d2f",
                                        soort = "Voor",
                                        actorNaam = "Volt",
                                        actorFractie = "Volt",
                                        fractieGrootte = 2,
                                    ),
                                ),
                            )
                        }
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
