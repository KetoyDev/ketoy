package com.developerstring.ketoy.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * A [KSerializer] for heterogeneous [Any] values that appear inside Ketoy
 * component property maps (`Map<String, Any>`).
 *
 * Because JSON is dynamically typed while Kotlin is not, Ketoy needs a single
 * serializer that can read **and** write the types used in server-driven UI
 * payloads: primitives, lists, maps, and data-class-like objects.
 *
 * ### Serialization behaviour
 * | Kotlin type       | Encoded as              |
 * |:------------------|:------------------------|
 * | [String]          | JSON string             |
 * | [Int]             | JSON integer            |
 * | [Long]            | JSON long               |
 * | [Float]           | JSON number (float)     |
 * | [Double]          | JSON number (double)    |
 * | [Boolean]         | JSON boolean            |
 * | [List]            | JSON array (recursive)  |
 * | [Map]             | JSON object (recursive) |
 * | *other*           | `toString()` string     |
 *
 * ### Deserialization behaviour
 * Uses [JsonDecoder] to inspect the actual JSON token type and produce
 * the matching Kotlin type: primitives, `List<Any>`, or `Map<String, Any>`.
 *
 * @see KComponentInfo
 * @see KNode
 */
object AnyValueSerializer : KSerializer<Any> {

    /** Descriptor registered under the synthetic name `"AnyValue"`. */
    override val descriptor = buildClassSerialDescriptor("AnyValue")

    /**
     * Encodes [value] into the appropriate JSON type.
     * Supports primitives, [List], and [Map] recursively.
     */
    override fun serialize(encoder: Encoder, value: Any) {
        val jsonEncoder = encoder as? JsonEncoder
        if (jsonEncoder != null) {
            jsonEncoder.encodeJsonElement(toJsonElement(value))
        } else {
            // Fallback for non-JSON encoders
            when (value) {
                is String  -> encoder.encodeString(value)
                is Int     -> encoder.encodeInt(value)
                is Long    -> encoder.encodeLong(value)
                is Float   -> encoder.encodeFloat(value)
                is Boolean -> encoder.encodeBoolean(value)
                is Double  -> encoder.encodeDouble(value)
                else       -> encoder.encodeString(value.toString())
            }
        }
    }

    /**
     * Decodes the next JSON token into the matching Kotlin type.
     */
    override fun deserialize(decoder: Decoder): Any {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeString()
        return fromJsonElement(jsonDecoder.decodeJsonElement())
    }

    // ── Helpers ─────────────────────────────────────────

    /**
     * Converts any Kotlin value to a [JsonElement] recursively.
     */
    private fun toJsonElement(value: Any?): JsonElement = when (value) {
        null       -> JsonNull
        is String  -> JsonPrimitive(value)
        is Int     -> JsonPrimitive(value)
        is Long    -> JsonPrimitive(value)
        is Float   -> JsonPrimitive(value)
        is Double  -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is List<*> -> JsonArray(value.map { toJsonElement(it) })
        is Map<*, *> -> JsonObject(
            value.entries.associate { (k, v) -> k.toString() to toJsonElement(v) }
        )
        else -> JsonPrimitive(value.toString())
    }

    /**
     * Converts a [JsonElement] back to a Kotlin value recursively.
     */
    private fun fromJsonElement(element: JsonElement): Any = when (element) {
        is JsonPrimitive -> {
            when {
                element.isString -> element.content
                element.booleanOrNull != null -> element.boolean
                element.intOrNull != null -> element.int
                element.longOrNull != null -> element.long
                element.floatOrNull != null -> element.float
                element.doubleOrNull != null -> element.double
                else -> element.content
            }
        }
        is JsonArray -> element.map { fromJsonElement(it) }
        is JsonObject -> element.entries.associate { (k, v) -> k to fromJsonElement(v) }
        else -> element.toString()
    }
}
