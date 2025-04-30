package nl.parlementairemonitor.cache

import com.mongodb.client.model.Accumulators.first
import com.mongodb.client.model.Accumulators.push
import com.mongodb.client.model.Aggregates.addFields
import com.mongodb.client.model.Aggregates.group
import com.mongodb.client.model.Aggregates.lookup
import com.mongodb.client.model.Aggregates.unset
import com.mongodb.client.model.Aggregates.unwind
import com.mongodb.client.model.Field
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nl.parlementairemonitor.MongoDb
import nl.parlementairemonitor.model.Persoon
import org.bson.Document

object PersoonCache {

    val persoonMap: Map<String, Persoon> by lazy {
        val collection = MongoDb.getDatabase().getCollection("Persoon", Persoon::class.java)
        runBlocking {
            collection.aggregate<Persoon>(
                listOf(
                    lookup("FractieZetelPersoon", "_id", "persoon", "zetels"),
                    unwind("\$zetels"),
                    lookup("FractieZetel", "zetels.fractieZetel", "_id", "fractiezetel"),
                    unwind("\$fractiezetel"),
                    lookup("Fractie", "fractiezetel.fractie", "_id", "fractie"),
                    unwind("\$fractie"),
                    unset("fractie.aantalZetels"),
                    addFields(
                        Field(
                            "zetelCombined", Document(
                                "\$mergeObjects", listOf(
                                    "\$zetels",
                                    Document("fractie", "\$fractie"),
                                    Document("persoon", null)
                                )
                            )
                        )
                    ),
                    group(
                        "\$_id",
                        first("titels", "\$titels"),
                        first("initialen", "\$initialen"),
                        first("tussenvoegsel", "\$tussenvoegsel"),
                        first("achternaam", "\$achternaam"),
                        first("voornamen", "\$voornamen"),
                        first("roepnaam", "\$roepnaam"),
                        first("geslacht", "\$geslacht"),
                        first("functie", "\$functie"),
                        first("geboortedatum", "\$geboortedatum"),
                        first("geboorteplaats", "\$geboorteplaats"),
                        first("geboorteland", "\$geboorteland"),
                        first("overlijdensdatum", "\$overlijdensdatum"),
                        first("overlijdensplaats", "\$overlijdensplaats"),
                        first("woonplaats", "\$woonplaats"),
                        first("land", "\$land"),
                        push("zetels", "\$zetelCombined"),
                    )
                )
            ).toList().associateBy { it.id }
        }
    }
}