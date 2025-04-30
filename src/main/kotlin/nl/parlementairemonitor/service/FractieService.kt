package nl.parlementairemonitor.service

import com.mongodb.client.model.Filters
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.flow.firstOrNull
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.cache.FractieCache
import nl.parlementairemonitor.model.Fractie
import nl.parlementairemonitor.model.Zetel
import nl.parlementairemonitor.util.Filter
import org.bson.Document

object FractieService {
    private val collection by lazy {
        MongoDb.getDatabase().getCollection("Fractie", Fractie::class.java)
    }

    fun list(filter: Filter<Zetel>): List<Fractie> {
        return FractieCache.fractieMap.values
            .filter { it.zetels?.any(filter) ?: false }
            .map { it.copy(zetels = null, aantalZetels = it.zetels?.filter(filter)?.size) }
            .sortedBy { it.afkorting }
    }

    fun get(id: String, filter: Filter<Zetel>): Fractie {
        val fractie = FractieCache.fractieMap[id] ?:throw NotFoundException()
        val filteredZetels = fractie.zetels?.filter(filter)
        return fractie.copy(zetels = filteredZetels, aantalZetels = filteredZetels?.size ?: 0)
    }

    suspend fun image(id: String): Document {
        val doc = collection.find<Document>(Filters.eq("_id", id)).firstOrNull()
        return doc ?: throw NotFoundException()
    }
}