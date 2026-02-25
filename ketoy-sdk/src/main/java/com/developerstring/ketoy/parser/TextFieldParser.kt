/**
 * TextField-component parser for the Ketoy SDUI engine.
 *
 * Converts JSON descriptors into the Compose types needed to configure
 * Material 3 `TextField` / `OutlinedTextField` components — including
 * [TextStyle], [VisualTransformation], [KeyboardOptions], [KeyboardActions],
 * and [TextFieldColors].
 *
 * @see resolveKetoyColor
 * @see parseColor
 */
package com.developerstring.ketoy.parser

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.*

// ─── TextStyle ────────────────────────────────────────────────────

/**
 * Parses a JSON object into a Compose [TextStyle].
 *
 * Recognised keys: `color`, `fontSize`, `fontWeight` (`"normal"`, `"bold"`,
 * `"light"`, `"medium"`, `"semibold"`, `"black"`), `fontStyle` (`"normal"`,
 * `"italic"`), `fontFamily` (`"default"`, `"serif"`, `"sansSerif"`,
 * `"monospace"`, `"cursive"`), `letterSpacing`, `textDecoration` (`"none"`,
 * `"underline"`, `"lineThrough"`), `lineHeight`, `background`.
 *
 * @param textStyleObject the JSON descriptor.
 * @return the resulting [TextStyle]; unset properties fall back to
 *   [TextStyle.Default].
 */
fun parseTextStyle(textStyleObject: JsonObject): TextStyle {
    return TextStyle(
        color = parseColor(textStyleObject["color"]?.jsonPrimitive?.content),
        fontSize = textStyleObject["fontSize"]?.jsonPrimitive?.intOrNull?.sp
            ?: TextStyle.Default.fontSize,
        fontWeight = when (textStyleObject["fontWeight"]?.jsonPrimitive?.content) {
            "normal" -> FontWeight.Normal
            "bold" -> FontWeight.Bold
            "light" -> FontWeight.Light
            "medium" -> FontWeight.Medium
            "semibold" -> FontWeight.SemiBold
            "black" -> FontWeight.Black
            else -> TextStyle.Default.fontWeight
        },
        fontStyle = when (textStyleObject["fontStyle"]?.jsonPrimitive?.content) {
            "normal" -> FontStyle.Normal
            "italic" -> FontStyle.Italic
            else -> TextStyle.Default.fontStyle
        },
        fontFamily = when (textStyleObject["fontFamily"]?.jsonPrimitive?.content) {
            "default" -> FontFamily.Default
            "serif" -> FontFamily.Serif
            "sansSerif" -> FontFamily.SansSerif
            "monospace" -> FontFamily.Monospace
            "cursive" -> FontFamily.Cursive
            else -> TextStyle.Default.fontFamily
        },
        letterSpacing = textStyleObject["letterSpacing"]?.jsonPrimitive?.floatOrNull?.sp
            ?: TextStyle.Default.letterSpacing,
        textDecoration = when (textStyleObject["textDecoration"]?.jsonPrimitive?.content) {
            "none" -> TextDecoration.None
            "underline" -> TextDecoration.Underline
            "lineThrough" -> TextDecoration.LineThrough
            else -> TextStyle.Default.textDecoration
        },
        lineHeight = textStyleObject["lineHeight"]?.jsonPrimitive?.intOrNull?.sp
            ?: TextStyle.Default.lineHeight,
        background = parseColor(textStyleObject["background"]?.jsonPrimitive?.content)
    )
}

// ─── Visual Transformation ────────────────────────────────────────

/**
 * Parses a visual-transformation JSON object for text fields.
 *
 * Currently supports `type = "password"` with an optional `mask` character;
 * all other types return [VisualTransformation.None].
 *
 * @param visualTransObject the JSON descriptor (e.g. `{ "type": "password", "mask": "*" }`).
 * @return the corresponding [VisualTransformation].
 */
fun parseVisualTransformation(visualTransObject: JsonObject): VisualTransformation {
    return when (visualTransObject["type"]?.jsonPrimitive?.content) {
        "password" -> {
            val mask = visualTransObject["mask"]?.jsonPrimitive?.content?.firstOrNull() ?: '•'
            PasswordVisualTransformation(mask)
        }
        else -> VisualTransformation.None
    }
}

// ─── Keyboard Options ─────────────────────────────────────────────
/**
 * Parses a JSON object into Compose [KeyboardOptions].
 *
 * Recognised keys:
 * - `capitalization`: `"none"`, `"characters"`, `"words"`, `"sentences"`
 * - `autoCorrect`: boolean
 * - `keyboardType`: `"text"`, `"ascii"`, `"number"`, `"phone"`, `"uri"`,
 *   `"email"`, `"password"`, `"numberPassword"`, `"decimal"`
 * - `imeAction`: `"default"`, `"none"`, `"go"`, `"search"`, `"send"`,
 *   `"previous"`, `"next"`, `"done"`
 *
 * @param keyboardObject the JSON descriptor.
 * @return the configured [KeyboardOptions].
 */fun parseKeyboardOptions(keyboardObject: JsonObject): KeyboardOptions {
    val capitalization = when (keyboardObject["capitalization"]?.jsonPrimitive?.content) {
        "none" -> KeyboardCapitalization.None
        "characters" -> KeyboardCapitalization.Characters
        "words" -> KeyboardCapitalization.Words
        "sentences" -> KeyboardCapitalization.Sentences
        else -> KeyboardCapitalization.None
    }

    @Suppress("DEPRECATION")
    val autoCorrect = keyboardObject["autoCorrect"]?.jsonPrimitive?.booleanOrNull ?: true

    val keyboardType = when (keyboardObject["keyboardType"]?.jsonPrimitive?.content) {
        "text" -> KeyboardType.Text
        "ascii" -> KeyboardType.Ascii
        "number" -> KeyboardType.Number
        "phone" -> KeyboardType.Phone
        "uri" -> KeyboardType.Uri
        "email" -> KeyboardType.Email
        "password" -> KeyboardType.Password
        "numberPassword" -> KeyboardType.NumberPassword
        "decimal" -> KeyboardType.Decimal
        else -> KeyboardType.Text
    }

    val imeAction = when (keyboardObject["imeAction"]?.jsonPrimitive?.content) {
        "default" -> ImeAction.Default
        "none" -> ImeAction.None
        "go" -> ImeAction.Go
        "search" -> ImeAction.Search
        "send" -> ImeAction.Send
        "previous" -> ImeAction.Previous
        "next" -> ImeAction.Next
        "done" -> ImeAction.Done
        else -> ImeAction.Default
    }

    @Suppress("DEPRECATION")
    return KeyboardOptions(
        capitalization = capitalization,
        autoCorrect = autoCorrect,
        keyboardType = keyboardType,
        imeAction = imeAction
    )
}

// ─── Keyboard Actions ─────────────────────────────────────────────
/**
 * Parses a JSON object into Compose [KeyboardActions].
 *
 * When a key is present (`onDone`, `onGo`, `onNext`, `onPrevious`,
 * `onSearch`, `onSend`), a default action is wired — e.g. `onDone`
 * hides the keyboard and clears focus, `onNext` / `onPrevious` move
 * focus accordingly.
 *
 * @param keyboardActionsObject the JSON descriptor.
 * @return the configured [KeyboardActions].
 */@Composable
fun parseKeyboardActions(keyboardActionsObject: JsonObject): KeyboardActions {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    return KeyboardActions(
        onDone = keyboardActionsObject["onDone"]?.jsonPrimitive?.content?.let {
            { keyboardController?.hide(); focusManager.clearFocus() }
        },
        onGo = keyboardActionsObject["onGo"]?.jsonPrimitive?.content?.let { { } },
        onNext = keyboardActionsObject["onNext"]?.jsonPrimitive?.content?.let {
            { focusManager.moveFocus(FocusDirection.Next) }
        },
        onPrevious = keyboardActionsObject["onPrevious"]?.jsonPrimitive?.content?.let {
            { focusManager.moveFocus(FocusDirection.Previous) }
        },
        onSearch = keyboardActionsObject["onSearch"]?.jsonPrimitive?.content?.let { { } },
        onSend = keyboardActionsObject["onSend"]?.jsonPrimitive?.content?.let { { } }
    )
}

// ─── TextField Colours ────────────────────────────────────────────
/**
 * Parses a comprehensive JSON colour-scheme object into [TextFieldColors]
 * for Material 3 text fields.
 *
 * Covers all text, container, cursor, indicator, icon, label, placeholder,
 * supporting-text, prefix, and suffix colour slots across
 * **focused / unfocused / disabled / error** states.
 * All colour values are resolved via [resolveKetoyColor] (supports hex,
 * named colours, and `@theme/` tokens).
 *
 * @param colorsObject the JSON descriptor with colour keys.
 * @return the fully configured [TextFieldColors].
 * @see resolveKetoyColor
 */@Composable
fun parseTextFieldColors(colorsObject: JsonObject): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedTextColor = resolveKetoyColor(colorsObject["focusedTextColor"]?.jsonPrimitive?.content),
        unfocusedTextColor = resolveKetoyColor(colorsObject["unfocusedTextColor"]?.jsonPrimitive?.content),
        disabledTextColor = resolveKetoyColor(colorsObject["disabledTextColor"]?.jsonPrimitive?.content),
        errorTextColor = resolveKetoyColor(colorsObject["errorTextColor"]?.jsonPrimitive?.content),
        focusedContainerColor = resolveKetoyColor(colorsObject["focusedContainerColor"]?.jsonPrimitive?.content),
        unfocusedContainerColor = resolveKetoyColor(colorsObject["unfocusedContainerColor"]?.jsonPrimitive?.content),
        disabledContainerColor = resolveKetoyColor(colorsObject["disabledContainerColor"]?.jsonPrimitive?.content),
        errorContainerColor = resolveKetoyColor(colorsObject["errorContainerColor"]?.jsonPrimitive?.content),
        cursorColor = resolveKetoyColor(colorsObject["cursorColor"]?.jsonPrimitive?.content),
        errorCursorColor = resolveKetoyColor(colorsObject["errorCursorColor"]?.jsonPrimitive?.content),
        focusedIndicatorColor = resolveKetoyColor(colorsObject["focusedIndicatorColor"]?.jsonPrimitive?.content),
        unfocusedIndicatorColor = resolveKetoyColor(colorsObject["unfocusedIndicatorColor"]?.jsonPrimitive?.content),
        disabledIndicatorColor = resolveKetoyColor(colorsObject["disabledIndicatorColor"]?.jsonPrimitive?.content),
        errorIndicatorColor = resolveKetoyColor(colorsObject["errorIndicatorColor"]?.jsonPrimitive?.content),
        focusedLeadingIconColor = resolveKetoyColor(colorsObject["focusedLeadingIconColor"]?.jsonPrimitive?.content),
        unfocusedLeadingIconColor = resolveKetoyColor(colorsObject["unfocusedLeadingIconColor"]?.jsonPrimitive?.content),
        disabledLeadingIconColor = resolveKetoyColor(colorsObject["disabledLeadingIconColor"]?.jsonPrimitive?.content),
        errorLeadingIconColor = resolveKetoyColor(colorsObject["errorLeadingIconColor"]?.jsonPrimitive?.content),
        focusedTrailingIconColor = resolveKetoyColor(colorsObject["focusedTrailingIconColor"]?.jsonPrimitive?.content),
        unfocusedTrailingIconColor = resolveKetoyColor(colorsObject["unfocusedTrailingIconColor"]?.jsonPrimitive?.content),
        disabledTrailingIconColor = resolveKetoyColor(colorsObject["disabledTrailingIconColor"]?.jsonPrimitive?.content),
        errorTrailingIconColor = resolveKetoyColor(colorsObject["errorTrailingIconColor"]?.jsonPrimitive?.content),
        focusedLabelColor = resolveKetoyColor(colorsObject["focusedLabelColor"]?.jsonPrimitive?.content),
        unfocusedLabelColor = resolveKetoyColor(colorsObject["unfocusedLabelColor"]?.jsonPrimitive?.content),
        disabledLabelColor = resolveKetoyColor(colorsObject["disabledLabelColor"]?.jsonPrimitive?.content),
        errorLabelColor = resolveKetoyColor(colorsObject["errorLabelColor"]?.jsonPrimitive?.content),
        focusedPlaceholderColor = resolveKetoyColor(colorsObject["focusedPlaceholderColor"]?.jsonPrimitive?.content),
        unfocusedPlaceholderColor = resolveKetoyColor(colorsObject["unfocusedPlaceholderColor"]?.jsonPrimitive?.content),
        disabledPlaceholderColor = resolveKetoyColor(colorsObject["disabledPlaceholderColor"]?.jsonPrimitive?.content),
        errorPlaceholderColor = resolveKetoyColor(colorsObject["errorPlaceholderColor"]?.jsonPrimitive?.content),
        focusedSupportingTextColor = resolveKetoyColor(colorsObject["focusedSupportingTextColor"]?.jsonPrimitive?.content),
        unfocusedSupportingTextColor = resolveKetoyColor(colorsObject["unfocusedSupportingTextColor"]?.jsonPrimitive?.content),
        disabledSupportingTextColor = resolveKetoyColor(colorsObject["disabledSupportingTextColor"]?.jsonPrimitive?.content),
        errorSupportingTextColor = resolveKetoyColor(colorsObject["errorSupportingTextColor"]?.jsonPrimitive?.content),
        focusedPrefixColor = resolveKetoyColor(colorsObject["focusedPrefixColor"]?.jsonPrimitive?.content),
        unfocusedPrefixColor = resolveKetoyColor(colorsObject["unfocusedPrefixColor"]?.jsonPrimitive?.content),
        disabledPrefixColor = resolveKetoyColor(colorsObject["disabledPrefixColor"]?.jsonPrimitive?.content),
        errorPrefixColor = resolveKetoyColor(colorsObject["errorPrefixColor"]?.jsonPrimitive?.content),
        focusedSuffixColor = resolveKetoyColor(colorsObject["focusedSuffixColor"]?.jsonPrimitive?.content),
        unfocusedSuffixColor = resolveKetoyColor(colorsObject["unfocusedSuffixColor"]?.jsonPrimitive?.content),
        disabledSuffixColor = resolveKetoyColor(colorsObject["disabledSuffixColor"]?.jsonPrimitive?.content),
        errorSuffixColor = resolveKetoyColor(colorsObject["errorSuffixColor"]?.jsonPrimitive?.content)
    )
}
