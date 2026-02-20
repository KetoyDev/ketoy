package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  TextField properties
// ─────────────────────────────────────────────────────────────

/**
 * Server-driven properties for a Material 3 `TextField` composable.
 *
 * This is the primary model used by the Ketoy SDUI engine to render fully
 * configurable text-input fields from JSON. Every visual and behavioral
 * aspect of the text field — label, placeholder, icons, colors, keyboard
 * behavior, and validation state — can be declared on the server and
 * delivered to any Android client at runtime.
 *
 * @property value The current text value displayed in the field.
 * @property onValueChange Action identifier invoked when the user changes the text.
 * @property modifier Optional [KModifier] controlling layout, padding, size, and other decoration.
 * @property enabled Whether the text field accepts user input. Defaults to `true` on the client when `null`.
 * @property readOnly When `true`, the field displays its value but does not allow editing.
 * @property textStyle Optional [KTextStyle] applied to the input text (font, size, color, etc.).
 * @property labelContent A list of [KNode] elements rendered as the floating label.
 * @property placeholderContent A list of [KNode] elements shown when the field is empty.
 * @property leadingIconContent A list of [KNode] elements rendered before the input area.
 * @property trailingIconContent A list of [KNode] elements rendered after the input area.
 * @property prefixContent A list of [KNode] elements displayed as a non-editable prefix inside the field.
 * @property suffixContent A list of [KNode] elements displayed as a non-editable suffix inside the field.
 * @property supportingTextContent A list of [KNode] elements rendered below the text field (e.g., helper or error text).
 * @property isError When `true`, the field is rendered in its error state.
 * @property visualTransformation Optional [KVisualTransformation] for masking or formatting input (e.g., password dots).
 * @property keyboardOptions Optional [KKeyboardOptions] controlling the software keyboard type and IME actions.
 * @property keyboardActions Optional [KKeyboardActions] mapping IME action buttons to action identifiers.
 * @property singleLine When `true`, the field is constrained to a single line and horizontal scrolling.
 * @property maxLines Maximum number of visible lines. Ignored when [singleLine] is `true`.
 * @property minLines Minimum number of visible lines the field will occupy.
 * @property interactionSource Optional [KInteractionSource] for observing interaction events.
 * @property shape Shape token (e.g., `"RoundedCorner"`, `"CircleShape"`) applied to the field container.
 * @property colors Optional [KTextFieldColors] overriding default Material 3 color slots.
 * @see KNode
 * @see KModifier
 * @see KTextStyle
 * @see KVisualTransformation
 * @see KKeyboardOptions
 * @see KKeyboardActions
 * @see KTextFieldColors
 */
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

/**
 * Server-driven representation of a Compose `TextStyle`.
 *
 * Controls every typographic attribute of rendered text — from font family and
 * weight to geometric transforms and shadows. All properties are nullable so
 * that the server only needs to send the values it wants to override; the
 * client falls back to Material 3 theme defaults for anything left unset.
 *
 * @property color Text color as a color-token string (e.g., `"#FF0000"` or `"primary"`).
 * @property fontSize Font size in scalable pixels (sp).
 * @property fontWeight Weight token such as `"Bold"`, `"Normal"`, `"W500"`, etc.
 * @property fontFamily Font family name (e.g., `"Roboto"`, `"SansSerif"`).
 * @property fontStyle Style token — typically `"Normal"` or `"Italic"`.
 * @property letterSpacing Extra spacing between characters, in sp.
 * @property textDecoration Decoration token such as `"Underline"`, `"LineThrough"`, or `"None"`.
 * @property textAlign Alignment token — `"Start"`, `"Center"`, `"End"`, `"Justify"`, etc.
 * @property lineHeight Line height in sp.
 * @property background Background color token applied behind the text.
 * @property textGeometricTransform Optional [KTextGeometricTransform] for horizontal scaling or skewing.
 * @property localeList Locale tag(s) for locale-sensitive text shaping (e.g., `"en-US"`).
 * @property textDirection Text direction token — `"Ltr"`, `"Rtl"`, or `"Content"`.
 * @property shadow Optional [KTextShadow] for a drop shadow behind the text.
 * @see KTextGeometricTransform
 * @see KTextShadow
 * @see KTextFieldProps
 */
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

/**
 * Server-driven geometric transform applied to text glyphs.
 *
 * Maps directly to Compose's `TextGeometricTransform`, enabling the server to
 * horizontally scale or skew text without requiring a custom font.
 *
 * @property scaleX Horizontal scale factor applied to each glyph. `1.0` is normal width.
 * @property skewX Horizontal skew (shear) factor. Positive values slant text to the left.
 * @see KTextStyle
 */
@Serializable
data class KTextGeometricTransform(
    val scaleX: Float? = null,
    val skewX: Float? = null
)

/**
 * Server-driven shadow definition for text rendering.
 *
 * Translates to Compose's `Shadow` class, allowing the server to apply a
 * drop-shadow effect to any text element in the UI.
 *
 * @property color Shadow color as a color-token string (e.g., `"#80000000"`).
 * @property offsetX Horizontal offset of the shadow in density-independent pixels (dp).
 * @property offsetY Vertical offset of the shadow in dp.
 * @property blurRadius Blur radius applied to the shadow in dp. Larger values produce softer shadows.
 * @see KTextStyle
 */
@Serializable
data class KTextShadow(
    val color: String? = null,
    val offsetX: Float? = null,
    val offsetY: Float? = null,
    val blurRadius: Float? = null
)

/**
 * Server-driven visual transformation applied to text-field input.
 *
 * Controls how the raw input text is displayed to the user — for example,
 * replacing characters with dots for password fields or applying a
 * formatted mask for phone numbers and credit cards.
 *
 * @property type Transformation type token. Common values: `"Password"`, `"None"`, `"Custom"`.
 * @property mask The masking character used when [type] is `"Password"` (e.g., `'●'` or `'*'`).
 * @property customPattern A formatting pattern string used when [type] is `"Custom"` (e.g., `"(###) ###-####"` for phone numbers).
 * @see KTextFieldProps
 */
@Serializable
data class KVisualTransformation(
    val type: String? = null,
    val mask: Char? = null,
    val customPattern: String? = null
)

/**
 * Server-driven keyboard configuration for a text field.
 *
 * Maps to Compose's `KeyboardOptions`, giving the server full control over the
 * software keyboard layout, capitalization rules, and IME action button.
 *
 * @property capitalization Capitalization behavior token — `"None"`, `"Characters"`, `"Words"`, or `"Sentences"`.
 * @property autoCorrect Whether the keyboard should offer auto-correction suggestions.
 * @property keyboardType Keyboard type token — `"Text"`, `"Number"`, `"Phone"`, `"Email"`, `"Password"`, `"Uri"`, etc.
 * @property imeAction IME action token — `"Done"`, `"Go"`, `"Next"`, `"Previous"`, `"Search"`, `"Send"`, or `"None"`.
 * @property platformImeOptions Optional [KPlatformImeOptions] for platform-specific keyboard behavior.
 * @see KPlatformImeOptions
 * @see KTextFieldProps
 */
@Serializable
data class KKeyboardOptions(
    val capitalization: String? = null,
    val autoCorrect: Boolean? = null,
    val keyboardType: String? = null,
    val imeAction: String? = null,
    val platformImeOptions: KPlatformImeOptions? = null
)

/**
 * Platform-specific IME (Input Method Editor) options.
 *
 * Provides additional, Android-specific hints to the software keyboard that go
 * beyond the standard Compose `KeyboardOptions`. These are typically used for
 * fine-grained control required by certain OEM keyboards or accessibility needs.
 *
 * @property privateImeOptions Vendor-specific IME option string passed to `EditorInfo.privateImeOptions`.
 * @property autoCorrect Platform-level auto-correction override, independent of [KKeyboardOptions.autoCorrect].
 * @property showPersonalizedSuggestions Whether the keyboard should show personalized (learned) suggestions.
 * @see KKeyboardOptions
 */
@Serializable
data class KPlatformImeOptions(
    val privateImeOptions: String? = null,
    val autoCorrect: Boolean? = null,
    val showPersonalizedSuggestions: Boolean? = null
)

/**
 * Server-driven action handlers for software-keyboard IME actions.
 *
 * Each property holds an action identifier string that the Ketoy SDUI engine
 * dispatches when the user taps the corresponding IME button. Set the desired
 * action in [KKeyboardOptions.imeAction] and provide the matching handler here.
 *
 * @property onDone Action identifier triggered when the user taps the **Done** IME button.
 * @property onGo Action identifier triggered when the user taps the **Go** IME button.
 * @property onNext Action identifier triggered when the user taps the **Next** IME button.
 * @property onPrevious Action identifier triggered when the user taps the **Previous** IME button.
 * @property onSearch Action identifier triggered when the user taps the **Search** IME button.
 * @property onSend Action identifier triggered when the user taps the **Send** IME button.
 * @see KKeyboardOptions
 * @see KTextFieldProps
 */
@Serializable
data class KKeyboardActions(
    val onDone: String? = null,
    val onGo: String? = null,
    val onNext: String? = null,
    val onPrevious: String? = null,
    val onSearch: String? = null,
    val onSend: String? = null
)

/**
 * Lightweight reference to a shared interaction-source instance.
 *
 * In the Ketoy SDUI engine, interaction sources are managed on the client side.
 * The server uses the [id] to reference a named source so that multiple
 * components can observe the same stream of interaction events (press, hover,
 * focus, drag, etc.).
 *
 * @property id Unique identifier of the interaction source registered on the client.
 * @see KTextFieldProps
 */
@Serializable
data class KInteractionSource(val id: String? = null)

/**
 * Complete color palette for every visual state of a Material 3 `TextField`.
 *
 * Each color slot corresponds to a combination of a text-field sub-element
 * (text, container, indicator, icon, label, placeholder, supporting text,
 * prefix, suffix, cursor, selection) and a visual state (focused, unfocused,
 * disabled, error). All values are color-token strings (e.g., `"#RRGGBB"`,
 * `"#AARRGGBB"`, or a named theme token like `"primary"`).
 *
 * Only non-null values override the client's Material 3 theme defaults,
 * so the server can send a minimal patch rather than the full palette.
 *
 * @property focusedTextColor Text color when the field is focused.
 * @property unfocusedTextColor Text color when the field is not focused.
 * @property disabledTextColor Text color when the field is disabled.
 * @property errorTextColor Text color when the field is in an error state.
 * @property focusedContainerColor Container (background) color when focused.
 * @property unfocusedContainerColor Container color when not focused.
 * @property disabledContainerColor Container color when disabled.
 * @property errorContainerColor Container color when in an error state.
 * @property cursorColor Cursor color during normal input.
 * @property errorCursorColor Cursor color when the field is in an error state.
 * @property selectionColors Optional [KSelectionColors] for text-selection handles and highlight.
 * @property focusedIndicatorColor Bottom indicator (underline) color when focused.
 * @property unfocusedIndicatorColor Indicator color when not focused.
 * @property disabledIndicatorColor Indicator color when disabled.
 * @property errorIndicatorColor Indicator color when in an error state.
 * @property focusedLeadingIconColor Leading-icon tint when focused.
 * @property unfocusedLeadingIconColor Leading-icon tint when not focused.
 * @property disabledLeadingIconColor Leading-icon tint when disabled.
 * @property errorLeadingIconColor Leading-icon tint when in an error state.
 * @property focusedTrailingIconColor Trailing-icon tint when focused.
 * @property unfocusedTrailingIconColor Trailing-icon tint when not focused.
 * @property disabledTrailingIconColor Trailing-icon tint when disabled.
 * @property errorTrailingIconColor Trailing-icon tint when in an error state.
 * @property focusedLabelColor Label color when focused.
 * @property unfocusedLabelColor Label color when not focused.
 * @property disabledLabelColor Label color when disabled.
 * @property errorLabelColor Label color when in an error state.
 * @property focusedPlaceholderColor Placeholder color when focused.
 * @property unfocusedPlaceholderColor Placeholder color when not focused.
 * @property disabledPlaceholderColor Placeholder color when disabled.
 * @property errorPlaceholderColor Placeholder color when in an error state.
 * @property focusedSupportingTextColor Supporting-text color when focused.
 * @property unfocusedSupportingTextColor Supporting-text color when not focused.
 * @property disabledSupportingTextColor Supporting-text color when disabled.
 * @property errorSupportingTextColor Supporting-text color when in an error state.
 * @property focusedPrefixColor Prefix color when focused.
 * @property unfocusedPrefixColor Prefix color when not focused.
 * @property disabledPrefixColor Prefix color when disabled.
 * @property errorPrefixColor Prefix color when in an error state.
 * @property focusedSuffixColor Suffix color when focused.
 * @property unfocusedSuffixColor Suffix color when not focused.
 * @property disabledSuffixColor Suffix color when disabled.
 * @property errorSuffixColor Suffix color when in an error state.
 * @see KSelectionColors
 * @see KTextFieldProps
 */
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

/**
 * Server-driven colors for the text-selection UI.
 *
 * Controls the appearance of selection handles and the highlighted background
 * shown when the user selects text inside a text field.
 *
 * @property handleColor Color of the draggable selection handles (the "teardrops").
 * @property backgroundColor Highlight color drawn behind the selected text.
 * @see KTextFieldColors
 */
@Serializable
data class KSelectionColors(
    val handleColor: String? = null,
    val backgroundColor: String? = null
)
