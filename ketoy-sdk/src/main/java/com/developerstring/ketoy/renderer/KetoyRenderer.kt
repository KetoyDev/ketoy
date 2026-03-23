/**
 * Central rendering engine for the Ketoy Server-Driven UI (SDUI) library.
 *
 * This file contains the primary entry-points that convert a JSON UI
 * description into a live Jetpack Compose tree:
 *
 * - [JSONStringToUI] — accepts a raw JSON string and renders the full tree.
 * - [RenderComponent] — central dispatch that routes each [UIComponent] node
 *   to the appropriate specialised renderer (layout, widget, scaffold, etc.).
 * - [RenderContentSlotFromJson] — helper for rendering named content-slots
 *   (e.g. `label`, `icon`) that appear as JSON arrays.
 * - [RenderCustomWidgetParser] — bridge to the custom-widget extension system.
 *
 * ### Rendering pipeline
 * ```
 * JSON string
 *   → [JSONStringToUI]
 *     → [KetoyThemeProvider] (colour resolution)
 *       → [RenderComponent] (recursive dispatch)
 *         → LayoutRenderer / WidgetRenderer / ScaffoldRenderer / …
 * ```
 *
 * @see LayoutRenderer
 * @see WidgetRenderer
 * @see ScaffoldRenderer
 * @see TextFieldRenderer
 * @see ComponentRenderer
 * @see OnClickResolver
 */
package com.developerstring.ketoy.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.model.KetoyVariable
import com.developerstring.ketoy.registry.KComponentRegistry
import com.developerstring.ketoy.theme.KetoyColorScheme
import com.developerstring.ketoy.theme.KetoyThemeProvider
import com.developerstring.ketoy.widget.KetoyWidgetRegistry
import com.developerstring.ketoy.wire.KetoyWireFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * Lightweight serialisable model that mirrors a single node in a JSON-driven UI tree.
 *
 * Each node carries a [type] (e.g. `"column"`, `"text"`, `"scaffold"`), an optional
 * [props] object with component-specific configuration, and an optional list of
 * [children] nodes that are rendered recursively.
 *
 * @property type     The component type identifier (case-insensitive).
 * @property props    Optional JSON object containing component properties such as
 *                    modifiers, colours, text values, onClick handlers, etc.
 * @property children Optional child nodes rendered inside this component.
 */
@Serializable
data class UIComponent(
    val type: String,
    val props: JsonObject? = null,
    val children: List<UIComponent>? = null
)

/**
 * Primary entry-point: parses a JSON string and renders the full Compose UI tree.
 *
 * Call this composable from your Activity, Fragment, or any Compose host to
 * display a server-driven screen.
 *
 * ```kotlin
 * // Minimal usage
 * JSONStringToUI(value = jsonFromServer)
 *
 * // With a custom colour scheme
 * JSONStringToUI(value = jsonFromServer, colorScheme = myKetoyColors)
 * ```
 *
 * @param value       The JSON-encoded UI tree (a single [UIComponent] root).
 * @param colorScheme Optional [KetoyColorScheme] that powers `@theme/` colour
 *                    references inside the tree. When `null` (the default),
 *                    [KetoyThemeProvider] automatically derives one from the
 *                    current Material 3 theme.
 *
 * @see RenderComponent
 * @see KetoyThemeProvider
 */
private val ketoyJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

@Deprecated(
    message = "Use JSONBytesToUI() with compressed wire bytes. Plain JSON rendering is deprecated.",
    replaceWith = ReplaceWith("JSONBytesToUI(data, colorScheme)", "com.developerstring.ketoy.renderer.JSONBytesToUI")
)
@Composable
fun JSONStringToUI(
    value: String,
    colorScheme: KetoyColorScheme? = null,
) {
    val component = remember(value) {
        ketoyJson.decodeFromString<UIComponent>(value)
    }

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
 * Entry-point for rendering compressed wire-format bytes directly.
 *
 * Accepts bytes produced by [KetoyWireFormat.encode] — any combination of
 * gzip, MessagePack, key aliasing, and type compression is auto-detected
 * and decoded before rendering.
 *
 * ```kotlin
 * val wireBytes: ByteArray = fetchFromServer()
 * JSONBytesToUI(data = wireBytes)
 * ```
 *
 * @param data  Compressed wire bytes.
 * @param colorScheme Optional [KetoyColorScheme].
 * @see JSONStringToUI
 * @see KetoyWireFormat.autoDecode
 */
@Composable
fun JSONBytesToUI(
    data: ByteArray,
    colorScheme: KetoyColorScheme? = null,
) {
    val result = remember(data) {
        try {
            val element = KetoyWireFormat.autoDecode(data)
            Result.success(ketoyJson.decodeFromJsonElement<UIComponent>(element))
        } catch (e: Exception) {
            System.err.println("Ketoy: Failed to decode wire bytes (${data.size} bytes): ${e.javaClass.simpleName}: ${e.message}")
            Result.failure(e)
        }
    }

    val component = result.getOrNull()
    if (component != null) {
        if (colorScheme != null) {
            KetoyThemeProvider(colorScheme = colorScheme) {
                RenderComponent(component)
            }
        } else {
            KetoyThemeProvider {
                RenderComponent(component)
            }
        }
    } else {
        Text(
            "Ketoy: decode error — ${result.exceptionOrNull()?.message ?: "unknown"}",
            color = androidx.compose.ui.graphics.Color.Red
        )
    }
}

/**
 * Central dispatch — recursively routes a [UIComponent] to the appropriate
 * specialised renderer based on its [UIComponent.type].
 *
 * Supported type families:
 * - **Layouts** — `column`, `row`, `box`, `lazycolumn`, `lazyrow`
 * - **Widgets** — `text`, `textfield`, `button`, `spacer`, `card`, `image`,
 *   `icon`, `iconbutton`, `component`
 * - **Scaffold** — `scaffold`, `topappbar`, `bottomappbar`, `navigationbar`,
 *   `floatingactionbutton`, `snackbar`, `snackbarhost`,
 *   `navigationdraweritem`, `customnavigationitem`, `navigationrail`,
 *   `navigationrailitem`, `appbaraction`, `modalbottomsheet`
 * - **Data constructs** — `dataclass`, `enum` (register values into
 *   [KetoyVariableRegistry])
 * - **Fallback** — custom widget parsers via [KetoyWidgetRegistry] or
 *   registered components via [KComponentRegistry]
 *
 * @param component The [UIComponent] node to render.
 *
 * @see LayoutRenderer
 * @see WidgetRenderer
 * @see ScaffoldRenderer
 * @see ComponentRenderer
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

        // Render-time list iteration
        "datalist" -> {
            val props = component.props ?: JsonObject(emptyMap())
            val dataSource = props["dataSource"]?.jsonPrimitive?.content ?: ""
            val itemAlias = props["itemAlias"]?.jsonPrimitive?.content ?: "item"
            val count = (KetoyVariableRegistry.getValue("$dataSource.count") as? Number)?.toInt() ?: 0

            // Discover field names from the first item registered in the registry
            val fieldNames = if (count > 0) {
                val prefix = "$dataSource.0."
                KetoyVariableRegistry.getAllVariables().keys
                    .filter { it.startsWith(prefix) }
                    .map { it.removePrefix(prefix) }
            } else emptyList()

            for (i in 0 until count) {
                // Register alias variables so {{data:itemAlias:field}} resolves
                fieldNames.forEach { field ->
                    val value = KetoyVariableRegistry.getValue("$dataSource.$i.$field")
                    KetoyVariableRegistry.register(
                        KetoyVariable.Immutable("$itemAlias.$field", value ?: "")
                    )
                }
                component.children?.forEach { child -> RenderComponent(child) }
            }
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
 * Renders a content-slot encoded as a [JsonArray] of [UIComponent] nodes.
 *
 * Many Material 3 components expose named slots such as `label`, `icon`,
 * `leadingIcon`, `trailingIcon`, etc. In the Ketoy JSON schema these are
 * represented as JSON arrays. This helper decodes each element and delegates
 * to [RenderComponent].
 *
 * @param contentArray The JSON array whose elements are serialised [UIComponent] objects.
 *
 * @see RenderComponent
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
 *
 * This is the bridge between the Ketoy core renderer and the custom-widget
 * extension system. Third-party consumers can register their own widget types
 * via [KetoyWidgetRegistry] and provide a [KetoyWidgetParser] that knows how
 * to convert the JSON props into a typed model and emit Composable content.
 *
 * @param T         The model type produced by the parser.
 * @param parser    The [KetoyWidgetParser] instance that converts JSON to a
 *                  model and renders it.
 * @param component The [UIComponent] whose [UIComponent.props] will be passed
 *                  to the parser.
 *
 * @see KetoyWidgetRegistry
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
