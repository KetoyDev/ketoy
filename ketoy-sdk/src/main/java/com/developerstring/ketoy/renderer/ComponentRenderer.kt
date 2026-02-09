package com.developerstring.ketoy.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.developerstring.ketoy.registry.KComponentRegistry
import kotlinx.serialization.json.*

/**
 * Render a custom component via the `"component"` type.
 * The component name is specified in `props.componentName`.
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
 * Render a registered component found by its type name in the component registry.
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
