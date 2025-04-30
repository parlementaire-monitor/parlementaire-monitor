package nl.parlementairemonitor

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import nl.parlementairemonitor.cache.FractieCache
import nl.parlementairemonitor.cache.PersoonCache

object MongoDb {
    lateinit var client: MongoClient

    fun getDatabase(): MongoDatabase {
        return client.getDatabase("parlementairemonitor")
    }
}

fun Application.configureDatabases() {
    val connection: String = System.getenv("MONGODB_CONNECTION")
    MongoDb.client = MongoClient.create(connection)

    PersoonCache.persoonMap
    FractieCache.fractieMap

    monitor.subscribe(ApplicationStopped) {
        MongoDb.client.close()
    }
}
