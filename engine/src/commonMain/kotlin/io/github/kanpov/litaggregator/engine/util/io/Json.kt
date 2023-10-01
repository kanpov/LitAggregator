package io.github.kanpov.litaggregator.engine.util.io

import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

val jsonInstance = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

private const val ROOT_LIST_PATTERN = """{"values": ~}"""

fun <T : JsonElement> decodeJsonRootList(rootJson: String): List<T> {
    val finalJson = ROOT_LIST_PATTERN.replace("~", rootJson)
    return jsonInstance.decodeFromString(JsonObject.serializer(), finalJson).jCustomArray<T>("values")
}

typealias JsonInstant = @Serializable(with = InstantSerializer::class) Instant

// kotlinx.serialization doesn't have KSerializers for JVM time structures
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return TimeFormatters.iso.parse(decoder.decodeString(), Instant::from)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(TimeFormatters.iso.format(value))
    }
}

typealias JsonUuid = @Serializable(with = UuidSerializer::class) UUID

object UuidSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uuid", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

val LocalDateTime.asInstant: Instant
    get() = toInstant(ZoneOffset.ofHours(0))

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

// Quick (and not null-safe) access to non-modeled JSON data
fun <T : JsonElement> JsonObject.jCustomArray(name: String): List<T> {
    return this[name]!!.jsonArray.map { it as T }
}

fun JsonObject.jArray(name: String): List<JsonObject> {
    return jCustomArray(name)
}

fun JsonObject.jObject(name: String): JsonObject {
    return this[name]!!.jsonObject
}

fun JsonObject.jString(name: String): String {
    return this[name]!!.jsonPrimitive.content
}

fun JsonObject.jInt(name: String): Int {
    return this[name]!!.jsonPrimitive.int
}

fun JsonObject.jFloat(name: String): Float {
    return this[name]!!.jsonPrimitive.float
}

fun JsonObject.jBoolean(name: String): Boolean {
    return this[name]!!.jsonPrimitive.boolean
}

val JsonObject.asFullName: String
    get() = "${jString("last_name")} ${jString("first_name")} ${jString("middle_name")}"

//// Example: "2023-09-20 11:59:41"
//fun parseMeshTime(literal: String): Instant {
//    return (if (literal.length > 16) mtfDateTimeLong else mtfTimeShortFormatter).parseInstant(literal)
//}
//
//private val mtfTimeShortFormatter: DateTimeFormatter = DateTimeFormatter
//    .ofPattern("yyyy-MM-dd HH:mm")
//    .withZone(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(3)))
//
//private val mtfDateTimeLong: DateTimeFormatter = DateTimeFormatter
//    .ofPattern("yyyy-MM-dd HH:mm:ss")
//    .withZone(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(3)))
//
//fun DateTimeFormatter.parseInstant(literal: String): Instant = parse(literal, Instant::from)
//
//fun Instant.differenceFrom(other: Instant): Instant = Instant.ofEpochMilli(other.toEpochMilli() - this.toEpochMilli())