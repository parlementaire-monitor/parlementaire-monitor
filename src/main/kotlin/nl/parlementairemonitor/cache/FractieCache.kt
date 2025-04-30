package nl.parlementairemonitor.cache

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.model.Fractie
import org.bson.Document

object FractieCache {

    val fractieMap: Map<String, Fractie> by lazy {
        runBlocking {
            val collection = MongoDb.getDatabase().getCollection("Fractie", Fractie::class.java)
            collection.aggregate<Fractie>(
                listOf(
                    Aggregates.lookup("FractieZetel", "_id", "fractie", "fractiezetel"),
                    Aggregates.unwind("\$fractiezetel"),
                    Aggregates.lookup("FractieZetelPersoon", "fractiezetel._id", "fractieZetel", "fractiezetelpersoon"),
                    Aggregates.unwind("\$fractiezetelpersoon"),
                    Aggregates.lookup("Persoon", "fractiezetelpersoon.persoon", "_id", "persoon"),
                    Aggregates.unwind("\$persoon"),
                    Aggregates.addFields(
                        Field(
                            "zetelCombined", Document(
                                "\$mergeObjects",
                                listOf(
                                    "\$fractiezetelpersoon",
                                    Document("persoon", "\$persoon"),
                                    Document("fractie", null)
                                )
                            )
                        )
                    ),
                    Aggregates.group(
                        "\$_id",
                        Accumulators.first("afkorting", "\$afkorting"),
                        Accumulators.first("naamNl", "\$naamNl"),
                        Accumulators.first("naamEn", "\$naamEn"),
                        Accumulators.first("datumActief", "\$datumActief"),
                        Accumulators.first("datumInactief", "\$datumInactief"),
                        Accumulators.push("zetels", "\$zetelCombined"),
                    )
                )
            ).toList().associateBy { it.id }
        }
    }

}