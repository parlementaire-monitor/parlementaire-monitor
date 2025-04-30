package nl.parlementairemonitor.service

import com.mongodb.client.model.Aggregates.count
import com.mongodb.client.model.Aggregates.facet
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Aggregates.sort
import com.mongodb.client.model.Facet
import com.mongodb.client.model.Filters
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import nl.parlementairemonitor.JSON
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.model.Motie
import nl.parlementairemonitor.model.Paginated
import nl.parlementairemonitor.util.Pagination
import nl.parlementairemonitor.util.Pagination.SortDirection.DESC
import nl.parlementairemonitor.util.TextUtil
import org.bson.Document

object MotieService {
    private val collection by lazy {
        MongoDb.getDatabase().getCollection<Motie>("Motie")
    }

    suspend fun search(q: String, pagination: Pagination): Paginated<Motie> {

        val queryFilters = TextUtil.makeSearchableText(q)
            .split(" ")
            .map { Filters.regex("zoekTekst", it, "i") }
        val filter = if (queryFilters.size == 1) queryFilters.first() else Filters.and(queryFilters)

        val document = collection.aggregate<Document>(listOf(
            match(filter),
            sort(pagination.sort("datum", DESC)),
            facet(
                Facet("moties", pagination.facet()),
                Facet("total", count()),
            ),
        )).firstOrNull()

        val moties: List<Motie> = document
            ?.getList("moties", Document::class.java)
            ?.map { JSON.decodeFromString(it.toJson()) } ?: emptyList()

        val total = document
            ?.getList("total", Document::class.java)
            ?.firstOrNull()
            ?.getInteger("count") ?: 0

        return Paginated(
            data = moties.map { it.copy(volledigeTekst = null, documentactor = null, stemming = null) },
            count = total,
            page = pagination.page,
            size = pagination.size,
        )
    }

    suspend fun get(id: String): Motie {
        val motie = collection
            .find(Filters.eq("_id", id))
            .toList().firstOrNull()
        return motie ?: throw NotFoundException()
    }

}