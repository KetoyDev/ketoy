package com.developerstring.ketoy.core

import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.registry.KComponentRegistry
import com.developerstring.ketoy.util.KColors
import com.developerstring.ketoy.wire.KetoyWireFormat
import com.developerstring.ketoy.wire.WireFormatConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/** Compact (non-pretty-print) JSON instance for minified output. */
private val MinifiedJson: Json = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
}

/**
 * Convert a [KNode] to a compact JSON string.
 *
 * Used internally by the DSL rendering pipeline (local screen content,
 * composable fallback, asset loading) and by [ContentEntry.buildJson].
 * For **network transport** and **export**, prefer [toWireBytes] which
 * applies the full compression pipeline (10-15x smaller).
 */
fun KNode.toJson(): String {
    return KetoyJson.encodeToString(KNode.serializer(), this)
}

/**
 * Convert a [KNode] to a minified JSON string optimised for wire transfer.
 *
 * Unlike [toJson] (which uses pretty-print), this produces the smallest
 * valid JSON string — no whitespace, no indentation.
 */
fun KNode.toMinifiedJson(): String {
    return MinifiedJson.encodeToString(KNode.serializer(), this)
}

/**
 * Convert a [KNode] to compressed wire bytes using the full
 * Ketoy compression pipeline (key aliasing + type compression +
 * optional MessagePack + gzip).
 *
 * @param config Wire format configuration. Defaults to [WireFormatConfig.OPTIMIZED].
 * @return Compressed bytes ready for network transmission or cache storage.
 */
fun KNode.toWireBytes(config: WireFormatConfig = WireFormatConfig.OPTIMIZED): ByteArray {
    val minified = toMinifiedJson()
    return KetoyWireFormat.encode(minified, config)
}

/**
 * Parse compressed wire bytes back to a [KNode].
 *
 * Handles any combination of gzip, MessagePack, key aliasing, and type
 * compression. Uses auto-detection for maximum compatibility.
 *
 * @param data Wire bytes (potentially compressed and/or binary-encoded).
 * @return The decoded [KNode], or an error [KTextNode] on failure.
 */
fun parseKetoyWireBytes(data: ByteArray): KNode {
    return try {
        val element = KetoyWireFormat.autoDecode(data)
        KetoyJson.decodeFromJsonElement(KNode.serializer(), element)
    } catch (e: Exception) {
        KTextNode(
            KTextProps(
                text = "Failed to decode wire bytes: ${e.message}",
                color = KColors.Red,
                fontSize = 12
            )
        )
    }
}

/**
 * Convert a [KNode] to an enhanced JSON string that includes component metadata,
 * schema version, and required imports.
 *
 * @deprecated Use [toWireBytes] for all production and export scenarios.
 */
@Deprecated(
    message = "Use toWireBytes() for compressed wire format. Enhanced JSON is deprecated.",
    replaceWith = ReplaceWith("toWireBytes()", "com.developerstring.ketoy.core.toWireBytes")
)
fun KNode.toEnhancedJson(): String {
    val usedComponents = extractUsedComponents(this)
    val componentMetadata = usedComponents.mapNotNull { name ->
        KComponentRegistry.getMetadata(name)?.let { name to it }
    }.toMap()

    val schema = KetoyJsonSchema(
        ui = this,
        components = componentMetadata,
        requiredImports = componentMetadata.values.flatMap { it.imports }.distinct()
    )

    return KetoyJson.encodeToString(KetoyJsonSchema.serializer(), schema)
}

/**
 * Parse an enhanced-JSON string (with component metadata) back to a [KNode].
 * Falls back to plain JSON parsing on failure.
 *
 * @deprecated Use [parseKetoyWireBytes] for all production and import scenarios.
 */
@Deprecated(
    message = "Use parseKetoyWireBytes() for compressed wire format. Enhanced JSON parsing is deprecated.",
    replaceWith = ReplaceWith("parseKetoyWireBytes(data)", "com.developerstring.ketoy.core.parseKetoyWireBytes")
)
fun parseEnhancedJson(jsonString: String): KNode {
    return try {
        val schema = KetoyJson.decodeFromString<KetoyJsonSchema>(jsonString)
        schema.components.values.forEach { metadata ->
            if (!KComponentRegistry.isAvailable(metadata.name)) {
                KComponentRegistry.loadFromMetadata(metadata)
            }
        }
        schema.ui
    } catch (_: Exception) {
        try {
            KetoyJson.decodeFromString<KNode>(jsonString)
        } catch (e: Exception) {
            KTextNode(KTextProps(text = "Error parsing JSON: ${e.message}"))
        }
    }
}

/**
 * Parse a plain Ketoy JSON string to a [KNode].
 * Returns an error [KTextNode] on failure.
 *
 * @deprecated Use [parseKetoyWireBytes] for all production and import scenarios.
 */
@Deprecated(
    message = "Use parseKetoyWireBytes() for compressed wire format. Plain JSON parsing is deprecated.",
    replaceWith = ReplaceWith("parseKetoyWireBytes(data)", "com.developerstring.ketoy.core.parseKetoyWireBytes")
)
fun parseKetoyJson(jsonString: String): KNode {
    return try {
        KetoyJson.decodeFromString<KNode>(jsonString)
    } catch (e: Exception) {
        KTextNode(
            KTextProps(
                text = "Failed to parse JSON: ${e.message}",
                color = KColors.Red,
                fontSize = 12
            )
        )
    }
}

// ─── Internal helpers ─────────────────────────────────────────────

private fun extractUsedComponents(node: KNode): List<String> {
    val components = mutableListOf<String>()

    when (node) {
        is KComponentNode -> {
            val name = node.props.name.ifEmpty { node.props.componentName }
            if (name.isNotEmpty()) components.add(name)
        }
        is KColumnNode -> node.children.forEach { components.addAll(extractUsedComponents(it)) }
        is KRowNode -> node.children.forEach { components.addAll(extractUsedComponents(it)) }
        is KBoxNode -> node.children.forEach { components.addAll(extractUsedComponents(it)) }
        is KButtonNode -> node.children.forEach { components.addAll(extractUsedComponents(it)) }
        is KCardNode -> node.children.forEach { components.addAll(extractUsedComponents(it)) }
        is KLazyColumnNode -> node.children.forEach { components.addAll(extractUsedComponents(it)) }
        is KLazyRowNode -> node.children.forEach { components.addAll(extractUsedComponents(it)) }
        else -> { /* leaf nodes */ }
    }

    return components.distinct()
}
