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

fun parseKeyboardOptions(keyboardObject: JsonObject): KeyboardOptions {
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

@Composable
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

@Composable
fun parseTextFieldColors(colorsObject: JsonObject): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedTextColor = parseColor(colorsObject["focusedTextColor"]?.jsonPrimitive?.content),
        unfocusedTextColor = parseColor(colorsObject["unfocusedTextColor"]?.jsonPrimitive?.content),
        disabledTextColor = parseColor(colorsObject["disabledTextColor"]?.jsonPrimitive?.content),
        errorTextColor = parseColor(colorsObject["errorTextColor"]?.jsonPrimitive?.content),
        focusedContainerColor = parseColor(colorsObject["focusedContainerColor"]?.jsonPrimitive?.content),
        unfocusedContainerColor = parseColor(colorsObject["unfocusedContainerColor"]?.jsonPrimitive?.content),
        disabledContainerColor = parseColor(colorsObject["disabledContainerColor"]?.jsonPrimitive?.content),
        errorContainerColor = parseColor(colorsObject["errorContainerColor"]?.jsonPrimitive?.content),
        cursorColor = parseColor(colorsObject["cursorColor"]?.jsonPrimitive?.content),
        errorCursorColor = parseColor(colorsObject["errorCursorColor"]?.jsonPrimitive?.content),
        focusedIndicatorColor = parseColor(colorsObject["focusedIndicatorColor"]?.jsonPrimitive?.content),
        unfocusedIndicatorColor = parseColor(colorsObject["unfocusedIndicatorColor"]?.jsonPrimitive?.content),
        disabledIndicatorColor = parseColor(colorsObject["disabledIndicatorColor"]?.jsonPrimitive?.content),
        errorIndicatorColor = parseColor(colorsObject["errorIndicatorColor"]?.jsonPrimitive?.content),
        focusedLeadingIconColor = parseColor(colorsObject["focusedLeadingIconColor"]?.jsonPrimitive?.content),
        unfocusedLeadingIconColor = parseColor(colorsObject["unfocusedLeadingIconColor"]?.jsonPrimitive?.content),
        disabledLeadingIconColor = parseColor(colorsObject["disabledLeadingIconColor"]?.jsonPrimitive?.content),
        errorLeadingIconColor = parseColor(colorsObject["errorLeadingIconColor"]?.jsonPrimitive?.content),
        focusedTrailingIconColor = parseColor(colorsObject["focusedTrailingIconColor"]?.jsonPrimitive?.content),
        unfocusedTrailingIconColor = parseColor(colorsObject["unfocusedTrailingIconColor"]?.jsonPrimitive?.content),
        disabledTrailingIconColor = parseColor(colorsObject["disabledTrailingIconColor"]?.jsonPrimitive?.content),
        errorTrailingIconColor = parseColor(colorsObject["errorTrailingIconColor"]?.jsonPrimitive?.content),
        focusedLabelColor = parseColor(colorsObject["focusedLabelColor"]?.jsonPrimitive?.content),
        unfocusedLabelColor = parseColor(colorsObject["unfocusedLabelColor"]?.jsonPrimitive?.content),
        disabledLabelColor = parseColor(colorsObject["disabledLabelColor"]?.jsonPrimitive?.content),
        errorLabelColor = parseColor(colorsObject["errorLabelColor"]?.jsonPrimitive?.content),
        focusedPlaceholderColor = parseColor(colorsObject["focusedPlaceholderColor"]?.jsonPrimitive?.content),
        unfocusedPlaceholderColor = parseColor(colorsObject["unfocusedPlaceholderColor"]?.jsonPrimitive?.content),
        disabledPlaceholderColor = parseColor(colorsObject["disabledPlaceholderColor"]?.jsonPrimitive?.content),
        errorPlaceholderColor = parseColor(colorsObject["errorPlaceholderColor"]?.jsonPrimitive?.content),
        focusedSupportingTextColor = parseColor(colorsObject["focusedSupportingTextColor"]?.jsonPrimitive?.content),
        unfocusedSupportingTextColor = parseColor(colorsObject["unfocusedSupportingTextColor"]?.jsonPrimitive?.content),
        disabledSupportingTextColor = parseColor(colorsObject["disabledSupportingTextColor"]?.jsonPrimitive?.content),
        errorSupportingTextColor = parseColor(colorsObject["errorSupportingTextColor"]?.jsonPrimitive?.content),
        focusedPrefixColor = parseColor(colorsObject["focusedPrefixColor"]?.jsonPrimitive?.content),
        unfocusedPrefixColor = parseColor(colorsObject["unfocusedPrefixColor"]?.jsonPrimitive?.content),
        disabledPrefixColor = parseColor(colorsObject["disabledPrefixColor"]?.jsonPrimitive?.content),
        errorPrefixColor = parseColor(colorsObject["errorPrefixColor"]?.jsonPrimitive?.content),
        focusedSuffixColor = parseColor(colorsObject["focusedSuffixColor"]?.jsonPrimitive?.content),
        unfocusedSuffixColor = parseColor(colorsObject["unfocusedSuffixColor"]?.jsonPrimitive?.content),
        disabledSuffixColor = parseColor(colorsObject["disabledSuffixColor"]?.jsonPrimitive?.content),
        errorSuffixColor = parseColor(colorsObject["errorSuffixColor"]?.jsonPrimitive?.content)
    )
}
