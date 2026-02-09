package com.developerstring.ketoy.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.model.KetoyVariable
import com.developerstring.ketoy.registry.KComponentRegistry
import com.developerstring.ketoy.theme.KetoyColorScheme
import com.developerstring.ketoy.theme.KetoyThemeProvider
import com.developerstring.ketoy.widget.KetoyWidgetRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * Lightweight model used by the renderer to interpret JSON-driven UI trees.
 */
@Serializable
data class UIComponent(
    val type: String,
    val props: JsonObject? = null,
    val children: List<UIComponent>? = null
)

/**
 * Primary entry-point: parses a JSON string and renders the resulting tree.
 *
 * @param value  The JSON-encoded UI tree.
 * @param colorScheme  Optional [KetoyColorScheme] to power `@theme/` colour
 *   references.  When `null` (the default), [KetoyThemeProvider] will
 *   automatically derive one from the current [MaterialTheme].
 */
@Composable
fun JSONStringToUI(
    value: String,
    colorScheme: KetoyColorScheme? = null,
) {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    val component = jsonConfig.decodeFromString<UIComponent>(value)

    if (colorScheme != null) {
        KetoyThemeProvider(colorScheme = colorScheme) {
            RenderComponent(component)
        }
    } else {
        KetoyThemeProvider {
            RenderComponent(component)
        }
    }
}

/**
 * Central dispatch – routes a [UIComponent] to the appropriate renderer.
 */
@Composable
fun RenderComponent(component: UIComponent) {
    when (component.type.lowercase()) {
        // Layouts
        "column"    -> RenderColumn(component)
        "row"       -> RenderRow(component)
        "box"       -> RenderBox(component)
        "lazycolumn" -> RenderLazyColumn(component)
        "lazyrow"   -> RenderLazyRow(component)

        // Widgets
        "text"      -> RenderText(component)
        "textfield" -> RenderTextField(component)
        "button"    -> RenderButton(component)
        "spacer"    -> RenderSpacer(component)
        "card"      -> RenderCard(component)
        "image"     -> RenderImage(component)
        "icon"      -> RenderIcon(component)
        "iconbutton" -> RenderIconButton(component)
        "component" -> RenderCustomComponent(component)

        // Scaffold family
        "scaffold"             -> RenderScaffold(component)
        "topappbar"            -> RenderTopAppBar(component)
        "bottomappbar"         -> RenderBottomAppBar(component)
        "navigationbar"        -> RenderNavigationBar(component)
        "floatingactionbutton" -> RenderFloatingActionButton(component)
        "snackbar"             -> RenderSnackBar(component)
        "snackbarhost"         -> RenderSnackBarHost(component)
        "navigationdraweritem" -> RenderNavigationDrawerItem(component)
        "customnavigationitem" -> RenderCustomNavigationItem(component)
        "navigationrail"       -> RenderNavigationRail(component)
        "navigationrailitem"   -> RenderNavigationRailItem(component)
        "appbaraction"         -> RenderAppBarAction(component)
        "navigationbaritem"    -> {} // rendered by NavigationBar parent in RowScope
        "modalbottomsheet"     -> RenderModalBottomSheet(component)

        // Data constructs
        "dataclass" -> {
            val props = component.props ?: JsonObject(emptyMap())
            val id = props["id"]?.jsonPrimitive?.content ?: ""
            val fields = props["fields"]?.jsonObject ?: JsonObject(emptyMap())
            fields.forEach { (fieldName, value) ->
                val variableId = "${id}_$fieldName"
                val fieldValue = if (value is JsonPrimitive) value.content else value.toString()
                KetoyVariableRegistry.register(KetoyVariable.Immutable(variableId, fieldValue))
            }
        }
        "enum" -> {
            val props = component.props ?: JsonObject(emptyMap())
            val id = props["id"]?.jsonPrimitive?.content ?: ""
            val enumName = props["enumName"]?.jsonPrimitive?.content ?: ""
            val selectedValue = props["selectedValue"]?.jsonPrimitive?.content ?: ""
            val values = props["values"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            KetoyVariableRegistry.register(KetoyVariable.Mutable("${id}_selectedValue", selectedValue))
            KetoyVariableRegistry.register(KetoyVariable.Immutable("${id}_values", values))
            KetoyVariableRegistry.register(KetoyVariable.Immutable("${id}_enumName", enumName))
        }

        // Fallback – check custom widget parsers first, then legacy registry
        else -> {
            val name = component.type
            val widgetParser = KetoyWidgetRegistry.resolveParser(name)
                ?: KetoyWidgetRegistry.resolveParser(name.lowercase())

            if (widgetParser != null) {
                RenderCustomWidgetParser(widgetParser, component)
            } else if (KComponentRegistry.get(name) != null) {
                RenderRegisteredComponent(name, component)
            } else {
                Text("Unknown component: ${component.type}")
            }
        }
    }
}

/**
 * Renders content-slot arrays (e.g. `label`, `icon` slots in TextField / Scaffold).
 */
@Composable
fun RenderContentSlotFromJson(contentArray: JsonArray) {
    contentArray.forEach { contentElement ->
        val contentComponent = Json.decodeFromJsonElement<UIComponent>(contentElement)
        RenderComponent(contentComponent)
    }
}

/**
 * Renders a widget using a registered [KetoyWidgetParser].
 * This enables the Stac-like custom widget extension system.
 */
@Composable
@Suppress("UNCHECKED_CAST")
internal fun <T> RenderCustomWidgetParser(
    parser: com.developerstring.ketoy.widget.KetoyWidgetParser<T>,
    component: UIComponent
) {
    val json = component.props ?: JsonObject(emptyMap())
    val model = parser.getModel(json)
    parser.parse(model)
}
