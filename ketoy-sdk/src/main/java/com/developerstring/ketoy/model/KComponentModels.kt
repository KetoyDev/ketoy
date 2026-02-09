package com.developerstring.ketoy.model

import androidx.compose.runtime.Composable
import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Custom-component metadata & info
// ─────────────────────────────────────────────────────────────

@Serializable
data class KComponentInfo(
    val name: String,
    val packageName: String = "",
    val className: String = "",
    val description: String = "",
    val version: String = "1.0",
    val parameterTypes: Map<String, String> = emptyMap(),
    val requiredProps: List<String> = emptyList(),
    val optionalProps: List<String> = emptyList()
) {
    @Transient
    var renderer: (@Composable (Map<String, Any>) -> Unit)? = null
}

@Serializable
data class KComponentMetadata(
    val name: String,
    val packageName: String = "",
    val className: String = "",
    val imports: List<String> = emptyList(),
    val version: String = "1.0"
)

/**
 * Wrapper used when serializing a full Ketoy UI tree together with
 * component metadata so that a consumer can load missing components.
 */
@Serializable
data class KetoyJsonSchema(
    val ui: KNode,
    val components: Map<String, KComponentMetadata> = emptyMap(),
    val version: String = "1.0",
    val requiredImports: List<String> = emptyList(),
    val fallbackMode: String = "graceful"
)
