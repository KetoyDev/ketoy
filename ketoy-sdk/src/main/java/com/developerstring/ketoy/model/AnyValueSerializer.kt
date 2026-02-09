package com.developerstring.ketoy.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for [Any] values used inside component property maps.
 */
object AnyValueSerializer : KSerializer<Any> {
    override val descriptor = buildClassSerialDescriptor("AnyValue")

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

    override fun deserialize(decoder: Decoder): Any = decoder.decodeString()
}
