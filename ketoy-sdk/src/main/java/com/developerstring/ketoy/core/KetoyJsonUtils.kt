package com.developerstring.ketoy.core

import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.registry.KComponentRegistry
import com.developerstring.ketoy.util.KColors
import kotlinx.serialization.encodeToString

/**
 * Convert a [KNode] to a compact JSON string.
 */
fun KNode.toJson(): String {
    return KetoyJson.encodeToString(KNode.serializer(), this)
}

/**
 * Convert a [KNode] to an enhanced JSON string that includes component metadata,
 * schema version, and required imports.
 */
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
 */
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
 */
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
