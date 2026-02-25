package com.developerstring.ketoy.model

import androidx.compose.runtime.Composable
import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Custom-component metadata & info
// ─────────────────────────────────────────────────────────────

/**
 * Full registration record for a **custom component** in the Ketoy SDUI system.
 *
 * When your app provides its own Composable widgets beyond the built-in set,
 * register a [KComponentInfo] so the renderer can discover and invoke it.
 *
 * The optional [renderer] lambda is used at runtime to actually draw the
 * component; it is marked `@Transient` and therefore excluded from JSON
 * serialization.
 *
 * ### JSON wire-format example
 * ```json
 * {
 *   "name": "RatingBar",
 *   "packageName": "com.myapp.widgets",
 *   "className": "RatingBarWidget",
 *   "description": "A 5-star rating bar",
 *   "version": "1.2",
 *   "parameterTypes": { "rating": "Float", "maxStars": "Int" },
 *   "requiredProps": ["rating"],
 *   "optionalProps": ["maxStars"]
 * }
 * ```
 *
 * ### Kotlin usage
 * ```kotlin
 * val info = KComponentInfo(
 *     name = "RatingBar",
 *     parameterTypes = mapOf("rating" to "Float"),
 *     requiredProps = listOf("rating")
 * ).apply {
 *     renderer = { props ->
 *         val rating = (props["rating"] as? String)?.toFloatOrNull() ?: 0f
 *         MyRatingBar(rating = rating)
 *     }
 * }
 * ```
 *
 * @property name Unique component name that matches the `type` field in [KNode] payloads.
 * @property packageName Fully-qualified package where the real Composable lives (informational).
 * @property className Simple class name of the backing Composable (informational).
 * @property description A short human-readable summary of what this component does.
 * @property version Semver-style version string to enable backward-compatible evolution.
 * @property parameterTypes Map of property name → expected Kotlin type name (e.g. `"Int"`, `"String"`).
 * @property requiredProps List of property names that **must** be present in the JSON `props` map.
 * @property optionalProps List of property names that may be omitted (defaults apply).
 * @property renderer Runtime-only Composable lambda invoked by the Ketoy renderer with resolved
 *   `props`. Not serialized.
 * @see KComponentMetadata
 * @see KetoyJsonSchema
 * @see KNode
 */
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

/**
 * Lightweight metadata about a component, used inside [KetoyJsonSchema] to
 * declare which custom components a UI tree depends on.
 *
 * Unlike [KComponentInfo], this class carries no runtime renderer and is
 * purely descriptive – suitable for manifest files and dependency resolution.
 *
 * ### JSON wire-format example
 * ```json
 * {
 *   "name": "RatingBar",
 *   "packageName": "com.myapp.widgets",
 *   "className": "RatingBarWidget",
 *   "imports": ["com.myapp.widgets.RatingBarWidget"],
 *   "version": "1.2"
 * }
 * ```
 *
 * @property name Component name matching the `type` field in [KNode].
 * @property packageName Fully-qualified package of the component.
 * @property className Simple class name.
 * @property imports List of import statements a code-generator might need.
 * @property version Semver-style version string.
 * @see KComponentInfo
 * @see KetoyJsonSchema
 */
@Serializable
data class KComponentMetadata(
    val name: String,
    val packageName: String = "",
    val className: String = "",
    val imports: List<String> = emptyList(),
    val version: String = "1.0"
)

/**
 * Top-level envelope for a complete Ketoy Server-Driven UI payload.
 *
 * A [KetoyJsonSchema] bundles the UI tree ([ui]) with the metadata of every
 * custom component the tree references ([components]), plus versioning and
 * error-handling directives. This is the object your backend returns and your
 * app passes to the Ketoy renderer.
 *
 * ### JSON wire-format example
 * ```json
 * {
 *   "ui": { "type": "Column", "children": [ ... ] },
 *   "components": {
 *     "RatingBar": { "name": "RatingBar", "version": "1.2" }
 *   },
 *   "version": "1.0",
 *   "requiredImports": ["com.myapp.widgets.RatingBarWidget"],
 *   "fallbackMode": "graceful"
 * }
 * ```
 *
 * ### Kotlin usage
 * ```kotlin
 * val schema: KetoyJsonSchema = Json.decodeFromString(jsonString)
 * KetoyRenderer(schema.ui)
 * ```
 *
 * @property ui The root [KNode] of the UI tree to render.
 * @property components Map of component name → [KComponentMetadata] for every custom
 *   component referenced in [ui]. Built-in widgets need not be listed.
 * @property version Schema version string for forward-/backward-compatibility checks.
 * @property requiredImports Aggregated list of import paths that a consumer needs.
 * @property fallbackMode Strategy when an unknown component is encountered:
 *   `"graceful"` (render a placeholder) or `"strict"` (throw).
 * @see KNode
 * @see KComponentInfo
 * @see KComponentMetadata
 */
@Serializable
data class KetoyJsonSchema(
    val ui: KNode,
    val components: Map<String, KComponentMetadata> = emptyMap(),
    val version: String = "1.0",
    val requiredImports: List<String> = emptyList(),
    val fallbackMode: String = "graceful"
)
