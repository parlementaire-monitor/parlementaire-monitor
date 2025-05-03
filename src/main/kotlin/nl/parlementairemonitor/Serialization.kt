package nl.parlementairemonitor

import io.ktor.http.ContentType.Application.Json
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import org.bson.Document
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val JSON = Json {
    prettyPrint = false
    explicitNulls = false
    encodeDefaults = true
    ignoreUnknownKeys = true
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(JSON, Json.withCharset(Charsets.UTF_8))
    }
}

object TimestampSerializer : KSerializer<Long> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("Europe/Amsterdam"))

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TimestampSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Long) {
        val instant = Instant.ofEpochMilli(value)
        encoder.encodeString(formatter.format(instant))
    }

    override fun deserialize(decoder: Decoder): Long {
        return decoder.decodeLong()
    }
}

//@Serializable(with = BsonSerializer::class)
//class BsonMap(map: Map<String, *>) : Document(map)
//
//object BsonNSerializer : KSerializer<BsonMap> {
//    val jsonSerializer by lazy {
//        JSON.serializersModule.serializer(JsonObject::class.java)
//    }
////    override val descriptor: SerialDescriptor = SerialDescriptor()
//    override fun deserialize(decoder: Decoder): BsonMap {
//        val json = jsonSerializer.deserialize(decoder)
//        return BsonMap(
//            Document.parse(
//                JSON.encodeToString(json)
//            ).toMap()
//        )
//    }
//    override fun serialize(encoder: Encoder, value: BsonMap) {
//        encoder.encodeSerializableValue(
//            JsonElement.serializer(),
//            JSON.parseToJsonElement(value.toBsonDocument().toJson())
//        )
//    }
//}

object BsonSerializer : KSerializer<Document> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("BsonDocument")

    override fun serialize(encoder: Encoder, value: Document) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("BsonDocumentAsJsonObjectSerializer only supports JSON")

        val json = value.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build())
        val element = JSON.parseToJsonElement(json)
        jsonEncoder.encodeJsonElement(element)
    }

    override fun deserialize(decoder: Decoder): Document {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("BsonDocumentAsJsonObjectSerializer only supports JSON")

        val element = jsonDecoder.decodeJsonElement()
        return Document.parse(element.toString())
    }
}