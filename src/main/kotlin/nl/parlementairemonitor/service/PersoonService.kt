package nl.parlementairemonitor.service

import com.mongodb.client.model.Filters
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.flow.firstOrNull
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.cache.PersoonCache
import nl.parlementairemonitor.model.Persoon
import nl.parlementairemonitor.model.Zetel
import nl.parlementairemonitor.util.Filter
import org.bson.Document

object PersoonService {
    private val collection by lazy {
        MongoDb.getDatabase().getCollection("Persoon", Persoon::class.java)
    }

    fun list(filter: Filter<Zetel>): List<Persoon> {
        return PersoonCache.persoonMap.values
            .filter { it.zetels?.any(filter) ?: false }
            .map { it.copy(zetels = null, fractie = it.zetels?.firstOrNull()?.fractie?.afkorting) }
            .sortedBy { it.achternaam }
    }

    fun get(id: String, filter: Filter<Zetel>): Persoon {
        val persoon = PersoonCache.persoonMap[id] ?: throw NotFoundException()
        val zetels = persoon.zetels?.filter(filter)
        return persoon.copy(zetels = zetels, fractie = zetels?.firstOrNull()?.fractie?.afkorting)
    }

    suspend fun image(id: String): Document {
        val doc = collection.find<Document>(Filters.eq("_id", id)).firstOrNull()
        return doc ?: throw NotFoundException()
    }
}