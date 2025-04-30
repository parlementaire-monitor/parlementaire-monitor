package nl.parlementairemonitor.service

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.flow.toList
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.cache.PersoonCache
import nl.parlementairemonitor.model.Commissie
import nl.parlementairemonitor.model.CommissiePersoon
import nl.parlementairemonitor.model.CommissieZetel
import nl.parlementairemonitor.util.Filter

object CommissieService {
    private val database by lazy {
        MongoDb.getDatabase()
    }

    suspend fun list(filter: Filter<Commissie>): List<Commissie> {
        return database.getCollection<Commissie>("Commissie")
            .find()
            .sort(Sorts.ascending("afkorting"))
            .toList()
            .filter(filter)
    }

    suspend fun get(id: String, filter: Filter<CommissiePersoon>): Commissie {
        val commissie = database.getCollection<Commissie>("Commissie")
            .find(Filters.eq("_id", id))
            .toList()
            .firstOrNull() ?: throw NotFoundException("Commissie not found")
        val zetels = database.getCollection<CommissieZetel>("CommissieZetel")
            .aggregate(
                listOf(
                    Aggregates.match(Filters.eq("commissie", id)),
                    Aggregates.lookup(
                        "CommissieZetelVastPersoon",
                        "_id",
                        "commissieZetel",
                        "vast"
                    ),
                    Aggregates.lookup(
                        "CommissieZetelVervangerPersoon",
                        "_id",
                        "commissieZetel",
                        "vervanger"
                    )
                )
            )
            .toList()
        return commissie.copy(zetels = zetels
            .filter { it.vast.any(filter) }
            .map { it.copy(
                vast = it.vast.filter(filter)
                    .map { p -> p.copy(naam = PersoonCache.persoonMap[p.persoon]?.displayName()) },
                vervanger = it.vervanger.filter(filter)
                    .map { p -> p.copy(naam = PersoonCache.persoonMap[p.persoon]?.displayName()) })
            }
        )
    }
}