package io.github.kanpov.litaggregator.engine.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

val jsonInstance = Json {
    prettyPrint = true
    encodeDefaults = true
}

private const val ROOT_LIST_PATTERN = """{"values": ~}"""

private data class RootList<T>(
    val values: List<T>
)

fun <T> decodeJsonRootList(rootJson: String): List<T> {
    val finalJson = ROOT_LIST_PATTERN.replace("~", rootJson)
    return jsonInstance.decodeFromString<RootList<T>>(finalJson).values
}

typealias JsonInstant = @Serializable(with = InstantSerializer::class) Instant

// kotlinx.serialization doesn't have KSerializers for JVM time structures
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return DateTimeFormatter.ISO_DATE_TIME.parse(decoder.decodeString(), Instant::from)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(DateTimeFormatter.ISO_DATE_TIME.format(value))
    }
}

typealias JsonUuid = @Serializable(with = UuidSerializer::class) UUID

object UuidSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uuid", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

typealias FlagBoolean = @Serializable(with = FlagBooleanSerializer::class) Boolean

object FlagBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlagBoolean", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeInt() == 1
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(if (value) 1 else 0)
    }
}
