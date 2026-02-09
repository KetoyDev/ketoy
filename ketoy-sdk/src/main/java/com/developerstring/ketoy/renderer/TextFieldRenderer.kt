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
