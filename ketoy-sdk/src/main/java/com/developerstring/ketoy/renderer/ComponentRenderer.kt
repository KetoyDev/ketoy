/**
 * Custom-component renderers for the Ketoy SDUI library.
 *
 * This file bridges the server-driven rendering pipeline with user-registered
 * components that live in [KComponentRegistry]. Two entry-points are provided:
 *
 * - [RenderCustomComponent] — handles the `"component"` type by looking up
 *   `props.componentName` in the registry.
 * - [RenderRegisteredComponent] — handles the fallback path where the
 *   component’s type name itself is the registry key.
 *
 * Properties from the JSON payload are extracted into `Map<String, Any>` and
 * forwarded to the registered renderer lambda.
 *
 * @see KComponentRegistry
 * @see RenderComponent
 * @see UIComponent
 */
package com.developerstring.ketoy.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.developerstring.ketoy.registry.KComponentRegistry
import kotlinx.serialization.json.*

/**
 * Renders a custom component referenced by `props.componentName`.
 *
 * This is the handler for the `"component"` type in the [UIComponent] tree.
 * It reads the `componentName` from props, looks up the corresponding renderer
 * in [KComponentRegistry], extracts the nested `properties` object into a
 * flat `Map<String, Any>`, and invokes the renderer.
 *
 * Displays an error text if:
 * - No `componentName` is specified.
 * - The named component has not been registered.
 *
 * @param component The [UIComponent] whose `type` is `"component"`.
 *
 * @see KComponentRegistry
 * @see RenderRegisteredComponent
 */
@Composable
internal fun RenderCustomComponent(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val componentName = props["componentName"]?.jsonPrimitive?.content ?: ""

    if (componentName.isEmpty()) {
        Text("Error: No component name specified")
        return
    }

    val componentInfo = KComponentRegistry.get(componentName)
    if (componentInfo == null) {
        Text("Error: Component '$componentName' not registered")
        return
    }

    val properties = extractProperties(props["properties"]?.jsonObject)
    componentInfo.renderer?.let { it(properties) }
}

/**
 * Renders a registered component found by its raw type name.
 *
 * This is the fallback path used by [RenderComponent] when the type does not
 * match any built-in renderer. The type string is used directly as the
 * [KComponentRegistry] key. All props (except `modifier`) are flattened into
 * a `Map<String, Any>` for the renderer lambda.
 *
 * @param componentName The type name used as the [KComponentRegistry] key.
 * @param component     The full [UIComponent] node.
 *
 * @see KComponentRegistry
 * @see RenderCustomComponent
 */
@Composable
internal fun RenderRegisteredComponent(componentName: String, component: UIComponent) {
    val componentInfo = KComponentRegistry.get(componentName)
    if (componentInfo == null) {
        Text("Error: Component '$componentName' not registered")
        return
    }

    val props = component.props ?: JsonObject(emptyMap())
    val properties = mutableMapOf<String, Any>()

    props.forEach { (key, value) ->
        if (key != "modifier") {
            when (value) {
                is JsonPrimitive -> {
                    when {
                        value.isString -> properties[key] = value.content
                        value.booleanOrNull != null -> properties[key] = value.boolean
                        value.intOrNull != null -> properties[key] = value.int
                        value.floatOrNull != null -> properties[key] = value.float
                        value.doubleOrNull != null -> properties[key] = value.double
                        else -> properties[key] = value.content
                    }
                }
                else -> properties[key] = value.toString()
            }
        }
    }

    componentInfo.renderer?.let { it(properties) }
}

// ─── Private helpers ──────────────────────────────────────────────
/**
 * Flattens a [JsonObject] into a `Map<String, Any>` by extracting:
 * - Strings, booleans, ints, floats, and doubles from [JsonPrimitive] values.
 * - `toString()` representations for complex (non-primitive) values.
 *
 * @param propsObject The JSON object to extract, or `null`.
 * @return A flat map of property name to Kotlin value.
 */
private fun extractProperties(propsObject: JsonObject?): Map<String, Any> {
    if (propsObject == null) return emptyMap()
    val result = mutableMapOf<String, Any>()
    propsObject.forEach { (key, value) ->
        when (value) {
            is JsonPrimitive -> {
                when {
                    value.isString -> result[key] = value.content
                    value.booleanOrNull != null -> result[key] = value.boolean
                    value.intOrNull != null -> result[key] = value.int
                    value.floatOrNull != null -> result[key] = value.float
                    value.doubleOrNull != null -> result[key] = value.double
                    else -> result[key] = value.content
                }
            }
            else -> result[key] = value.toString()
        }
    }
    return result
}
