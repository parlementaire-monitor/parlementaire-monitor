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
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
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

