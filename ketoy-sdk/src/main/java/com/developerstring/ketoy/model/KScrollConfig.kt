package com.developerstring.ketoy.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Configuration for scroll behavior on a single axis (vertical or horizontal).
 *
 * [KScrollConfig] allows fine-grained control over scrolling including direction,
 * fling physics, and enabled state. It supports two JSON wire formats for
 * convenience:
 *
 * ### Simple boolean format (shorthand)
 * ```json
 * { "verticalScroll": true }
 * ```
 * Equivalent to `KScrollConfig(enabled = true)`.
 *
 * ### Full object format
 * ```json
 * {
 *   "verticalScroll": {
 *     "enabled": true,
 *     "reverseScrolling": true,
 *     "flingBehavior": "default"
 *   }
 * }
 * ```
 *
 * @property enabled Whether scrolling is enabled. Defaults to `true`.
 * @property reverseScrolling When `true`, reverses the scroll direction so that
 *   scrolling down moves content up (or vice versa). Defaults to `false`.
 * @property flingBehavior Fling behavior preset. Supported values:
 *   - `null` or `"default"` — Standard Android fling deceleration.
 *   - `"none"` — No fling; scrolling stops immediately when touch is released.
 *   Future versions may support additional presets like `"snap"`.
 *
 * @see KModifier.verticalScroll
 * @see KModifier.horizontalScroll
 */
@Serializable(with = KScrollConfigSerializer::class)
data class KScrollConfig(
    val enabled: Boolean = true,
    val reverseScrolling: Boolean = false,
    val flingBehavior: String? = null
) {
    companion object {
        /** Scroll config that enables scrolling with default behavior. */
        val Default = KScrollConfig(enabled = true)

        /** Scroll config that disables scrolling. */
        val Disabled = KScrollConfig(enabled = false)

        /** Fling behavior preset: default Android fling physics. */
        const val FLING_DEFAULT = "default"

        /** Fling behavior preset: no fling, stops immediately. */
        const val FLING_NONE = "none"
    }
}

/**
 * Custom serializer for [KScrollConfig] that handles both boolean shorthand
 * (`true`/`false`) and full object format.
 *
 * ### Deserialization
 * - `true` → `KScrollConfig(enabled = true)`
 * - `false` → `KScrollConfig(enabled = false)`
 * - `{ "enabled": true, ... }` → Full [KScrollConfig] object
 *
 * ### Serialization
 * Always serializes to the full object format for consistency.
 */
object KScrollConfigSerializer : KSerializer<KScrollConfig> {

    override val descriptor = buildClassSerialDescriptor("KScrollConfig") {
        element<Boolean>("enabled")
        element<Boolean>("reverseScrolling")
        element<String?>("flingBehavior")
    }

    override fun serialize(encoder: Encoder, value: KScrollConfig) {
        val jsonEncoder = encoder as? JsonEncoder
        if (jsonEncoder != null) {
            // Serialize as object
            val obj = buildJsonObject {
                put("enabled", value.enabled)
                if (value.reverseScrolling) {
                    put("reverseScrolling", true)
                }
                value.flingBehavior?.let { put("flingBehavior", it) }
            }
            jsonEncoder.encodeJsonElement(obj)
        } else {
            // Fallback for non-JSON encoders: encode as string representation
            encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): KScrollConfig {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return KScrollConfig.Default

        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> {
                // Boolean shorthand: true/false
                val enabled = element.booleanOrNull ?: true
                KScrollConfig(enabled = enabled)
            }
            is JsonObject -> {
                // Full object format
                val enabled = element["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
                val reverseScrolling = element["reverseScrolling"]?.jsonPrimitive?.booleanOrNull ?: false
                val flingBehavior = element["flingBehavior"]?.jsonPrimitive?.contentOrNull
                KScrollConfig(enabled, reverseScrolling, flingBehavior)
            }
            else -> KScrollConfig.Default
        }
    }
}
