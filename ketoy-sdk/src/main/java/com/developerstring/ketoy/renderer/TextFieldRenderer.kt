/**
 * TextField renderer for the Ketoy SDUI library.
 *
 * Converts a `"textfield"` [UIComponent] node into a Material 3 `TextField`
 * composable with full support for content slots, keyboard configuration,
 * visual transformations, and two-way data binding via [KetoyVariableRegistry].
 *
 * @see RenderTextField
 * @see UIComponent
 */
package com.developerstring.ketoy.renderer

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.parser.*
import kotlinx.serialization.json.*

/**
 * Renders a Material 3 `TextField` from a [UIComponent] node.
 *
 * The initial value is resolved through [KetoyVariableRegistry.resolveTemplate]
 * so that `{{variable}}` placeholders are evaluated at render time. On each
 * keystroke the optional `onValueChange` action ID is looked up in
 * [ActionRegistry] and invoked with the new text.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `value` | string | `""` | Initial text (supports `{{var}}` templates) |
 * | `enabled` | boolean | `true` | Whether the field is editable |
 * | `readOnly` | boolean | `false` | Display-only mode |
 * | `singleLine` | boolean | `false` | Restrict to a single line |
 * | `maxLines` | int | ∞/1 | Maximum visible lines |
 * | `minLines` | int | `1` | Minimum visible lines |
 * | `isError` | boolean | `false` | Error-state indicator |
 * | `textStyle` | object | default | Custom text style |
 * | `visualTransformation` | object | none | E.g. password masking |
 * | `keyboardOptions` | object | default | IME & keyboard type |
 * | `keyboardActions` | object | default | IME action callbacks |
 * | `colors` | object | defaults | TextField colour overrides |
 * | `onValueChange` | string | – | Action ID in [ActionRegistry] for text changes |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * ### Content slots (all optional JSON arrays)
 * `labelContent`, `placeholderContent`, `leadingIconContent`,
 * `trailingIconContent`, `prefixContent`, `suffixContent`,
 * `supportingTextContent`
 *
 * @param component The [UIComponent] whose `type` is `"textfield"`.
 *
 * @see RenderContentSlotFromJson
 * @see KetoyVariableRegistry
 * @see ActionRegistry
 */

@Composable
internal fun RenderTextField(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())

    val initialValue = KetoyVariableRegistry.resolveTemplate(
        props["value"]?.jsonPrimitive?.content ?: ""
    )
    var value by remember { mutableStateOf(initialValue) }
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val readOnly = props["readOnly"]?.jsonPrimitive?.booleanOrNull ?: false
    val singleLine = props["singleLine"]?.jsonPrimitive?.booleanOrNull ?: false
    val maxLines = props["maxLines"]?.jsonPrimitive?.intOrNull ?: if (singleLine) 1 else Int.MAX_VALUE
    val minLines = props["minLines"]?.jsonPrimitive?.intOrNull ?: 1
    val isError = props["isError"]?.jsonPrimitive?.booleanOrNull ?: false

    // Content slots
    val labelContent = props["labelContent"]?.jsonArray
    val placeholderContent = props["placeholderContent"]?.jsonArray
    val leadingIconContent = props["leadingIconContent"]?.jsonArray
    val trailingIconContent = props["trailingIconContent"]?.jsonArray
    val prefixContent = props["prefixContent"]?.jsonArray
    val suffixContent = props["suffixContent"]?.jsonArray
    val supportingTextContent = props["supportingTextContent"]?.jsonArray

    val textStyle = props["textStyle"]?.jsonObject?.let { parseTextStyle(it) } ?: TextStyle.Default
    val visualTransformation = props["visualTransformation"]?.jsonObject?.let { parseVisualTransformation(it) }
        ?: VisualTransformation.None
    val keyboardOptions = props["keyboardOptions"]?.jsonObject?.let { parseKeyboardOptions(it) }
        ?: KeyboardOptions.Default
    val keyboardActions = props["keyboardActions"]?.jsonObject?.let { parseKeyboardActions(it) }
        ?: KeyboardActions.Default
    val colors = props["colors"]?.jsonObject?.let { parseTextFieldColors(it) }
        ?: TextFieldDefaults.colors()

    TextField(
        value = value,
        onValueChange = { newValue ->
            value = newValue
            props["onValueChange"]?.jsonPrimitive?.content?.let { actionId ->
                ActionRegistry.getTextChange(actionId)?.invoke(newValue)
            }
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = labelContent?.let { { RenderContentSlotFromJson(it) } },
        placeholder = placeholderContent?.let { { RenderContentSlotFromJson(it) } },
        leadingIcon = leadingIconContent?.let { { RenderContentSlotFromJson(it) } },
        trailingIcon = trailingIconContent?.let { { RenderContentSlotFromJson(it) } },
        prefix = prefixContent?.let { { RenderContentSlotFromJson(it) } },
        suffix = suffixContent?.let { { RenderContentSlotFromJson(it) } },
        supportingText = supportingTextContent?.let { { RenderContentSlotFromJson(it) } },
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        colors = colors
    )
}
