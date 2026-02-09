package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  TextField properties
// ─────────────────────────────────────────────────────────────

@Serializable
data class KTextFieldProps(
    val value: String = "",
    val onValueChange: String? = null,
    val modifier: KModifier? = null,
    val enabled: Boolean? = null,
    val readOnly: Boolean? = null,
    val textStyle: KTextStyle? = null,
    val labelContent: List<KNode>? = null,
    val placeholderContent: List<KNode>? = null,
    val leadingIconContent: List<KNode>? = null,
    val trailingIconContent: List<KNode>? = null,
    val prefixContent: List<KNode>? = null,
    val suffixContent: List<KNode>? = null,
    val supportingTextContent: List<KNode>? = null,
    val isError: Boolean? = null,
    val visualTransformation: KVisualTransformation? = null,
    val keyboardOptions: KKeyboardOptions? = null,
    val keyboardActions: KKeyboardActions? = null,
    val singleLine: Boolean? = null,
    val maxLines: Int? = null,
    val minLines: Int? = null,
    val interactionSource: KInteractionSource? = null,
    val shape: String? = null,
    val colors: KTextFieldColors? = null
)

@Serializable
data class KTextStyle(
    val color: String? = null,
    val fontSize: Int? = null,
    val fontWeight: String? = null,
    val fontFamily: String? = null,
    val fontStyle: String? = null,
    val letterSpacing: Float? = null,
    val textDecoration: String? = null,
    val textAlign: String? = null,
    val lineHeight: Int? = null,
    val background: String? = null,
    val textGeometricTransform: KTextGeometricTransform? = null,
    val localeList: String? = null,
    val textDirection: String? = null,
    val shadow: KTextShadow? = null
)

@Serializable
data class KTextGeometricTransform(
    val scaleX: Float? = null,
    val skewX: Float? = null
)

@Serializable
data class KTextShadow(
    val color: String? = null,
    val offsetX: Float? = null,
    val offsetY: Float? = null,
    val blurRadius: Float? = null
)

@Serializable
data class KVisualTransformation(
    val type: String? = null,
    val mask: Char? = null,
    val customPattern: String? = null
)

@Serializable
data class KKeyboardOptions(
    val capitalization: String? = null,
    val autoCorrect: Boolean? = null,
    val keyboardType: String? = null,
    val imeAction: String? = null,
    val platformImeOptions: KPlatformImeOptions? = null
)

@Serializable
data class KPlatformImeOptions(
    val privateImeOptions: String? = null,
    val autoCorrect: Boolean? = null,
    val showPersonalizedSuggestions: Boolean? = null
)

@Serializable
data class KKeyboardActions(
    val onDone: String? = null,
    val onGo: String? = null,
    val onNext: String? = null,
    val onPrevious: String? = null,
    val onSearch: String? = null,
    val onSend: String? = null
)

@Serializable
data class KInteractionSource(val id: String? = null)

@Serializable
data class KTextFieldColors(
    val focusedTextColor: String? = null,
    val unfocusedTextColor: String? = null,
    val disabledTextColor: String? = null,
    val errorTextColor: String? = null,
    val focusedContainerColor: String? = null,
    val unfocusedContainerColor: String? = null,
    val disabledContainerColor: String? = null,
    val errorContainerColor: String? = null,
    val cursorColor: String? = null,
    val errorCursorColor: String? = null,
    val selectionColors: KSelectionColors? = null,
    val focusedIndicatorColor: String? = null,
    val unfocusedIndicatorColor: String? = null,
    val disabledIndicatorColor: String? = null,
    val errorIndicatorColor: String? = null,
    val focusedLeadingIconColor: String? = null,
    val unfocusedLeadingIconColor: String? = null,
    val disabledLeadingIconColor: String? = null,
    val errorLeadingIconColor: String? = null,
    val focusedTrailingIconColor: String? = null,
    val unfocusedTrailingIconColor: String? = null,
    val disabledTrailingIconColor: String? = null,
    val errorTrailingIconColor: String? = null,
    val focusedLabelColor: String? = null,
    val unfocusedLabelColor: String? = null,
    val disabledLabelColor: String? = null,
    val errorLabelColor: String? = null,
    val focusedPlaceholderColor: String? = null,
    val unfocusedPlaceholderColor: String? = null,
    val disabledPlaceholderColor: String? = null,
    val errorPlaceholderColor: String? = null,
    val focusedSupportingTextColor: String? = null,
    val unfocusedSupportingTextColor: String? = null,
    val disabledSupportingTextColor: String? = null,
    val errorSupportingTextColor: String? = null,
    val focusedPrefixColor: String? = null,
    val unfocusedPrefixColor: String? = null,
    val disabledPrefixColor: String? = null,
    val errorPrefixColor: String? = null,
    val focusedSuffixColor: String? = null,
    val unfocusedSuffixColor: String? = null,
    val disabledSuffixColor: String? = null,
    val errorSuffixColor: String? = null
)

@Serializable
data class KSelectionColors(
    val handleColor: String? = null,
    val backgroundColor: String? = null
)
