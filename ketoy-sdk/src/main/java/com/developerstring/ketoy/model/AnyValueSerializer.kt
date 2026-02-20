package com.developerstring.ketoy.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A [KSerializer] for heterogeneous [Any] values that appear inside Ketoy
 * component property maps (`Map<String, Any>`).
 *
 * Because JSON is dynamically typed while Kotlin is not, Ketoy needs a single
 * serializer that can read **and** write the five primitive types used in
 * server-driven UI payloads: [String], [Int], [Float], [Double], and [Boolean].
 *
 * ### Serialization behaviour
 * | Kotlin type | Encoded as          |
 * |:------------|:--------------------|
 * | [String]    | JSON string         |
 * | [Int]       | JSON integer        |
 * | [Float]     | JSON number (float) |
 * | [Double]    | JSON number (double)|
 * | [Boolean]   | JSON boolean        |
 * | *other*     | `toString()` string |
 *
 * ### Deserialization behaviour
 * All values are decoded as [String]. Callers that need a concrete numeric or
 * boolean type should parse the returned string themselves.
 *
 * ### Kotlin usage
 * ```kotlin
 * @Serializable
 * data class MyProps(
 *     val attrs: Map<String, @Serializable(with = AnyValueSerializer::class) Any>
 * )
 * ```
 *
 * @see KComponentInfo
 * @see KNode
 */
object AnyValueSerializer : KSerializer<Any> {

    /** Descriptor registered under the synthetic name `"AnyValue"`. */
    override val descriptor = buildClassSerialDescriptor("AnyValue")

    /**
     * Encodes [value] into the appropriate JSON primitive.
     *
     * @param encoder The encoder provided by kotlinx.serialization.
     * @param value The value to serialize – must be one of the supported
     *   primitive types listed above.
     */
    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is String  -> encoder.encodeString(value)
            is Int     -> encoder.encodeInt(value)
            is Float   -> encoder.encodeFloat(value)
            is Boolean -> encoder.encodeBoolean(value)
            is Double  -> encoder.encodeDouble(value)
            else       -> encoder.encodeString(value.toString())
        }
    }

    /**
     * Decodes the next JSON token as a [String].
     *
     * @param decoder The decoder provided by kotlinx.serialization.
     * @return The decoded string representation of the value.
     */
    override fun deserialize(decoder: Decoder): Any = decoder.decodeString()
}
