package com.developerstring.ketoy.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.model.KetoyVariable
import com.developerstring.ketoy.registry.KComponentRegistry
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
 */
@Composable
fun JSONStringToUI(value: String) {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    val component = jsonConfig.decodeFromString<UIComponent>(value)
    RenderComponent(component)
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

        // Data constructs
        "dataclass" -> {
            val props = component.props ?: JsonObject(emptyMap())
            val id = props["id"]?.jsonPrimitive?.content ?: ""
            val fields = props["fields"]?.jsonObject ?: JsonObject(emptyMap())
            fields.forEach { (fieldName, value) ->
                val variableId = "${id}_$fieldName"
                val fieldValue = value.jsonPrimitive?.content ?: value.toString()
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

        // Fallback – check custom registry
        else -> {
            val name = component.type
            if (KComponentRegistry.get(name) != null) {
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
