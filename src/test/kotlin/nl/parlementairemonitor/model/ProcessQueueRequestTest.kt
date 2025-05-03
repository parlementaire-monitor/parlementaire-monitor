package nl.parlementairemonitor.model

import nl.parlementairemonitor.JSON
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProcessQueueRequestTest {

    val json: String = """
        {
          "collection": "Document",
          "filter": {
            "soort": { "${'$'}eq": "Motie" },
            "${'$'}or": [
              { "active": true },
              { "role": "admin" }
            ]
          },
          "delay": "15m",
          "dryRun": true
        }
    """.trimIndent()

    val request = ProcessQueueRequest(
        collection = "Document",
        filter = Document(
            mapOf(
                "soort" to mapOf("\$eq" to "Motie"),
                "\$or" to listOf(
                    mapOf("active" to true),
                    mapOf("role" to "admin"),
                )
            )
        ),
        delay = "15m",
        dryRun = true,
    )

    @Test
    fun `test serialize Dpocument to string`() {

        val filter = JSON.encodeToString(ProcessQueueRequest.serializer(), request)
        assertEquals(129, filter.length)
    }

    @Test
    fun `test deserialize String to Document`() {
        val filter = JSON.decodeFromString<ProcessQueueRequest>(json)
        assertEquals(request.collection, filter.collection)
        assertEquals(request.filter.toBsonDocument(), filter.filter.toBsonDocument())
        assertEquals(request.delay, filter.delay)
        assertEquals(request.dryRun, filter.dryRun)
    }
}