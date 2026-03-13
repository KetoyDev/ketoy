package com.developerstring.ketoy.model

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

// ─────────────────────────────────────────────────────────────
//  Layout component properties
// ─────────────────────────────────────────────────────────────

/**
 * Property bag for a vertical layout container — the Compose `Column` equivalent.
 *
 * Holds the modifier, vertical arrangement, and horizontal alignment that
 * [KColumnNode] forwards to
 * [LayoutRenderer.RenderColumn][com.developerstring.ketoy.renderer.LayoutRenderer]
 * at render time.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "modifier": { "padding": { "all": 16 } },
 *   "verticalArrangement": "SpaceBetween",
 *   "horizontalAlignment": "CenterHorizontally"
 * }
 * ```
 *
 * @property modifier             Optional [KModifier] controlling size, padding, background, etc.
 * @property verticalArrangement  Compose `Arrangement.Vertical` name (e.g. `"Top"`, `"SpaceBetween"`).
 * @property horizontalAlignment  Compose `Alignment.Horizontal` name (e.g. `"Start"`, `"CenterHorizontally"`).
 * @see KColumnNode
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable
data class KColumnProps(
    val modifier: KModifier? = null,
    val verticalArrangement: String? = null,
    val horizontalAlignment: String? = null
)

/**
 * Property bag for a horizontal layout container — the Compose `Row` equivalent.
 *
 * Holds the modifier, horizontal arrangement, and vertical alignment that
 * [KRowNode] forwards to
 * [LayoutRenderer.RenderRow][com.developerstring.ketoy.renderer.LayoutRenderer]
 * at render time.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "modifier": { "fillMaxWidth": true },
 *   "horizontalArrangement": "SpaceEvenly",
 *   "verticalAlignment": "CenterVertically"
 * }
 * ```
 *
 * @property modifier               Optional [KModifier] controlling size, padding, background, etc.
 * @property horizontalArrangement   Compose `Arrangement.Horizontal` name (e.g. `"Start"`, `"SpaceEvenly"`).
 * @property verticalAlignment       Compose `Alignment.Vertical` name (e.g. `"Top"`, `"CenterVertically"`).
 * @see KRowNode
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable
data class KRowProps(
    val modifier: KModifier? = null,
    val horizontalArrangement: String? = null,
    val verticalAlignment: String? = null
)

/**
 * Property bag for a stacking layout container — the Compose `Box` equivalent.
 *
 * Overlays its children on top of each other with an optional
 * [contentAlignment] controlling the default position of each child.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "modifier": { "size": 200 },
 *   "contentAlignment": "Center"
 * }
 * ```
 *
 * @property modifier          Optional [KModifier] controlling size, padding, background, etc.
 * @property contentAlignment  Compose `Alignment` name (e.g. `"Center"`, `"TopStart"`).
 * @see KBoxNode
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable
data class KBoxProps(
    val modifier: KModifier? = null,
    val contentAlignment: String? = null
)

/**
 * Property bag for a vertically-scrolling lazy list — the Compose `LazyColumn` equivalent.
 *
 * Supports all the tuning knobs that `LazyColumn` exposes: arrangement, alignment,
 * scroll direction, content padding, and the number of items rendered beyond
 * visible bounds for smoother scrolling.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "verticalArrangement": "spacedBy:8",
 *   "horizontalAlignment": "CenterHorizontally",
 *   "reverseLayout": false,
 *   "userScrollEnabled": true,
 *   "contentPadding": { "horizontal": 16, "vertical": 8 },
 *   "beyondBoundsItemCount": 2
 * }
 * ```
 *
 * @property modifier               Optional [KModifier] controlling size, padding, background, etc.
 * @property verticalArrangement     Compose `Arrangement.Vertical` name or `"spacedBy:<dp>"` shorthand.
 * @property horizontalAlignment     Compose `Alignment.Horizontal` name.
 * @property userScrollEnabled       When `false`, user scroll gestures are disabled.
 * @property reverseLayout           When `true`, items are laid out bottom-to-top.
 * @property contentPadding          [KPadding] applied around the list content.
 * @property beyondBoundsItemCount   Extra items composed beyond the visible viewport for prefetching.
 * @see KLazyColumnNode
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable
data class KLazyColumnProps(
    val modifier: KModifier? = null,
    val verticalArrangement: String? = null,
    val horizontalAlignment: String? = null,
    val userScrollEnabled: Boolean? = null,
    val reverseLayout: Boolean? = null,
    val contentPadding: KPadding? = null,
    val beyondBoundsItemCount: Int? = null
)

/**
 * Property bag for a horizontally-scrolling lazy list — the Compose `LazyRow` equivalent.
 *
 * Mirrors the configuration surface of [KLazyColumnProps] but scrolls horizontally.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "horizontalArrangement": "spacedBy:12",
 *   "verticalAlignment": "CenterVertically",
 *   "reverseLayout": false,
 *   "userScrollEnabled": true,
 *   "contentPadding": { "horizontal": 8 },
 *   "beyondBoundsItemCount": 1
 * }
 * ```
 *
 * @property modifier               Optional [KModifier] controlling size, padding, background, etc.
 * @property horizontalArrangement   Compose `Arrangement.Horizontal` name or `"spacedBy:<dp>"` shorthand.
 * @property verticalAlignment       Compose `Alignment.Vertical` name.
 * @property userScrollEnabled       When `false`, user scroll gestures are disabled.
 * @property reverseLayout           When `true`, items are laid out end-to-start.
 * @property contentPadding          [KPadding] applied around the list content.
 * @property beyondBoundsItemCount   Extra items composed beyond the visible viewport for prefetching.
 * @see KLazyRowNode
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable
data class KLazyRowProps(
    val modifier: KModifier? = null,
    val horizontalArrangement: String? = null,
    val verticalAlignment: String? = null,
    val userScrollEnabled: Boolean? = null,
    val reverseLayout: Boolean? = null,
    val contentPadding: KPadding? = null,
    val beyondBoundsItemCount: Int? = null
)

// ─────────────────────────────────────────────────────────────
//  Leaf-widget properties
// ─────────────────────────────────────────────────────────────

/**
 * Property bag for a text label — the Compose `Text` equivalent.
 *
 * Encodes all visual and layout attributes of a single text run.
 * Colours are expressed as hex strings (e.g. `"#FF5722"`) and resolved at
 * render time.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "text": "Hello, World!",
 *   "fontSize": 18,
 *   "fontWeight": "Bold",
 *   "color": "#212121",
 *   "textAlign": "Center",
 *   "maxLines": 2,
 *   "overflow": "Ellipsis"
 * }
 * ```
 *
 * @property text           The display string. Defaults to an empty string.
 * @property modifier       Optional [KModifier] controlling size, padding, background, etc.
 * @property fontSize       Text size in scale-independent pixels (sp).
 * @property fontWeight     Compose `FontWeight` name (e.g. `"Bold"`, `"W400"`).
 * @property color          Text colour as a hex string (e.g. `"#FF5722"`).
 * @property textAlign      Compose `TextAlign` name (e.g. `"Start"`, `"Center"`, `"Justify"`).
 * @property maxLines       Maximum number of visible lines before truncation.
 * @property overflow       Compose `TextOverflow` name (e.g. `"Ellipsis"`, `"Clip"`).
 * @property letterSpacing  Additional space between characters, in sp.
 * @property lineHeight     Line height factor, in sp.
 * @see KTextNode
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable
data class KTextProps(
    val text: String = "",
    val modifier: KModifier? = null,
    val fontSize: Int? = null,
    val fontWeight: String? = null,
    val color: String? = null,
    val textAlign: String? = null,
    val maxLines: Int? = null,
    val overflow: String? = null,
    val letterSpacing: Float? = null,
    val lineHeight: Float? = null
)

/**
 * Property bag for a clickable button — the Compose `Button` equivalent.
 *
 * Carries styling, interaction callback key, and optional shape/elevation overrides.
 * The [onClick] string is a function key resolved at render time by the host
 * application's action handler.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "onClick": "submitForm",
 *   "enabled": true,
 *   "containerColor": "#6200EE",
 *   "contentColor": "#FFFFFF",
 *   "elevation": 4,
 *   "shape": "RoundedCornerShape(8)"
 * }
 * ```
 *
 * @property modifier       Optional [KModifier] controlling size, padding, background, etc.
 * @property onClick        Action key dispatched to the host when the button is tapped.
 * @property enabled        Whether the button is interactive. Defaults to Compose's own default when `null`.
 * @property containerColor Background colour as a hex string.
 * @property contentColor   Content (text/icon) colour as a hex string.
 * @property elevation      Shadow elevation in dp.
 * @property shape          Compose `Shape` descriptor (e.g. `"RoundedCornerShape(12)"`).
 * @see KButtonNode
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable
data class KButtonProps(
    val modifier: KModifier? = null,
    val onClick: JsonElement = JsonPrimitive("function"),
    val enabled: Boolean? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val elevation: Int? = null,
    val shape: String? = null
)

/**
 * Property bag for an empty spacing element — the Compose `Spacer` equivalent.
 *
 * Use [width] and [height] as convenient shorthands; for more complex sizing,
 * configure the [modifier] directly.
 *
 * ## JSON Wire Format
 * ```json
 * { "width": 0, "height": 16 }
 * ```
 *
 * @property modifier  Optional [KModifier] (typically used for advanced sizing).
 * @property width     Spacer width in dp; ignored when `null`.
 * @property height    Spacer height in dp; ignored when `null`.
 * @see KSpacerNode
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable
data class KSpacerProps(
    val modifier: KModifier? = null,
    val width: Int? = null,
    val height: Int? = null
)

/**
 * Property bag for a Material 3 card surface — the Compose `Card` equivalent.
 *
 * Wraps its children in an elevated, shaped container with optional click handling.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "shape": "RoundedCornerShape(16)",
 *   "containerColor": "#FFFFFF",
 *   "contentColor": "#000000",
 *   "elevation": 8,
 *   "border": { "width": 1, "color": "#E0E0E0" },
 *   "onClick": "openDetail",
 *   "enabled": true
 * }
 * ```
 *
 * @property modifier       Optional [KModifier] controlling size, padding, background, etc.
 * @property shape          Compose `Shape` descriptor (e.g. `"RoundedCornerShape(16)"`).
 * @property containerColor Card background colour as a hex string.
 * @property contentColor   Default content colour for children inside the card.
 * @property elevation      Shadow elevation in dp.
 * @property border         Optional [KBorder] defining the card's outline stroke.
 * @property onClick        Action key dispatched when the card is tapped; `null` means non-clickable.
 * @property enabled        Whether click interaction is enabled.
 * @see KCardNode
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable
data class KCardProps(
    val modifier: KModifier? = null,
    val shape: String? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val elevation: Int? = null,
    val border: KBorder? = null,
    val onClick: JsonElement? = null,
    val enabled: Boolean? = null
)

/**
 * Property bag for a raster or vector image — the Compose `Image` / `AsyncImage` equivalent.
 *
 * The image origin is described by [KImageSource], which can point to a URL,
 * a local resource, or a Base-64-encoded payload. The [scaleType] maps to
 * Compose `ContentScale` values via [KScaleType] constants.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "source": { "url": "https://example.com/hero.webp" },
 *   "contentDescription": "Hero banner",
 *   "scaleType": "centerCrop"
 * }
 * ```
 *
 * @property source              Image origin descriptor; see [KImageSource].
 * @property modifier            Optional [KModifier] controlling size, padding, background, etc.
 * @property contentDescription   Accessibility text for screen readers.
 * @property scaleType           One of the [KScaleType] constants (e.g. `"fitCenter"`, `"centerCrop"`).
 * @see KImageNode
 * @see KScaleType
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable
data class KImageProps(
    val source: KImageSource? = null,
    val modifier: KModifier? = null,
    val contentDescription: String? = null,
    val scaleType: String? = KScaleType.FitCenter
)

/**
 * Property bag for a Material icon — the Compose `Icon` equivalent.
 *
 * The [icon] name is resolved against the Material Icons set at render time.
 * An optional [style] selects the icon variant (e.g. `"Outlined"`,
 * `"Rounded"`, `"Sharp"`).
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "icon": "Home",
 *   "size": 24,
 *   "color": "#424242",
 *   "style": "Outlined",
 *   "contentDescription": "Home icon"
 * }
 * ```
 *
 * @property icon                Material icon name (e.g. `"Home"`, `"Settings"`).
 * @property modifier            Optional [KModifier] controlling size, padding, background, etc.
 * @property size                Icon size in dp.
 * @property color               Icon tint colour as a hex string.
 * @property style               Material icon style variant (e.g. `"Outlined"`, `"Rounded"`).
 * @property contentDescription   Accessibility text for screen readers.
 * @see KIconNode
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable
data class KIconProps(
    val icon: String = "",
    val modifier: KModifier? = null,
    val size: Int? = null,
    val color: String? = null,
    val style: String? = null,
    val contentDescription: String? = null
)

/**
 * Property bag for a tappable icon button — the Compose `IconButton` equivalent.
 *
 * Combines an icon with a click surface. Supports distinct colours for the
 * enabled and disabled states, allowing full server-driven control of
 * interactive icon affordances.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "icon": "Favorite",
 *   "onClick": "toggleFavorite",
 *   "enabled": true,
 *   "iconSize": 24,
 *   "iconColor": "#E91E63",
 *   "iconStyle": "Filled",
 *   "containerColor": "#FAFAFA",
 *   "contentColor": "#212121",
 *   "disabledContainerColor": "#E0E0E0",
 *   "disabledContentColor": "#9E9E9E",
 *   "contentDescription": "Toggle favourite"
 * }
 * ```
 *
 * @property icon                    Material icon name (e.g. `"Favorite"`, `"Delete"`).
 * @property modifier                Optional [KModifier] controlling size, padding, background, etc.
 * @property onClick                 Action key dispatched to the host when tapped.
 * @property enabled                 Whether the button is interactive.
 * @property iconSize                Icon size in dp.
 * @property iconColor               Icon tint colour as a hex string.
 * @property iconStyle               Material icon style variant.
 * @property containerColor          Background colour in the enabled state.
 * @property contentColor            Content colour in the enabled state.
 * @property disabledContainerColor  Background colour in the disabled state.
 * @property disabledContentColor    Content colour in the disabled state.
 * @property contentDescription       Accessibility text for screen readers.
 * @see KIconButtonNode
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable
data class KIconButtonProps(
    val icon: String = "",
    val modifier: KModifier? = null,
    val onClick: JsonElement = JsonPrimitive("function"),
    val enabled: Boolean? = null,
    val iconSize: Int? = null,
    val iconColor: String? = null,
    val iconStyle: String? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val disabledContainerColor: String? = null,
    val disabledContentColor: String? = null,
    val contentDescription: String? = null
)

/**
 * Property bag for a custom, host-registered component.
 *
 * Enables the server to reference a native Composable that the host application
 * has registered with the Ketoy SDK. The renderer looks up [componentName]
 * (or the legacy [name] field) in the component registry and passes
 * [properties]/[props] as an opaque key-value map.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "componentName": "RatingBar",
 *   "props": { "rating": 4.5, "maxStars": 5 },
 *   "version": "1.0",
 *   "fallbackComponent": "Text"
 * }
 * ```
 *
 * @property name              Legacy component identifier (prefer [componentName]).
 * @property componentName     Canonical component name used for registry lookup.
 * @property properties        Key-value property map (alias of [props] for backward compatibility).
 * @property props             Key-value property map forwarded to the registered Composable.
 * @property modifier          Optional [KModifier] controlling size, padding, background, etc.
 * @property version           Semantic version of the component contract.
 * @property requiredImports   List of import paths the host must satisfy (informational).
 * @property fallbackComponent Optional fallback [KNode] type name rendered when the component is not registered.
 * @see KComponentNode
 * @see com.developerstring.ketoy.renderer.ComponentRenderer
 */
@Serializable
data class KComponentProps(
    val name: String = "",
    val componentName: String = "",
    val properties: Map<String, @Serializable(with = AnyValueSerializer::class) Any> = emptyMap(),
    val props: Map<String, @Serializable(with = AnyValueSerializer::class) Any> = emptyMap(),
    val modifier: KModifier? = null,
    val version: String = "1.0",
    val requiredImports: List<String> = emptyList(),
    val fallbackComponent: String? = null
)

// ─────────────────────────────────────────────────────────────
//  Data-class / Enum properties
// ─────────────────────────────────────────────────────────────

/**
 * Property bag for a server-defined data-class node.
 *
 * Represents an opaque structured-data payload that the host can bind to its
 * own data models. The [fields] map carries arbitrary key-value pairs
 * serialized via [AnyValueSerializer].
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "id": "user_42",
 *   "className": "UserProfile",
 *   "fields": { "name": "Ada", "age": 36, "verified": true }
 * }
 * ```
 *
 * @property id         Unique identifier for this data-class instance.
 * @property className  Fully-qualified or simple class name the host should map to.
 * @property fields     Arbitrary key-value payload deserialized with [AnyValueSerializer].
 * @see KDataClassNode
 * @see AnyValueSerializer
 */
@Serializable
data class KDataClassProps(
    val id: String = "",
    val className: String = "",
    val fields: Map<String, @Serializable(with = AnyValueSerializer::class) Any> = emptyMap()
)

/**
 * Property bag for a server-defined enumeration node.
 *
 * Models a fixed set of string choices with a current selection and an
 * optional callback key for selection changes. Useful for dropdowns,
 * segmented controls, or any multi-choice UI driven from the server.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "id": "theme_picker",
 *   "enumName": "AppTheme",
 *   "values": ["Light", "Dark", "System"],
 *   "selectedValue": "Dark",
 *   "onSelectionChange": "changeTheme"
 * }
 * ```
 *
 * @property id                 Unique identifier for this enum instance.
 * @property enumName           Display or class name for the enumeration.
 * @property values             The ordered list of allowed enum values.
 * @property selectedValue      The currently selected value.
 * @property onSelectionChange  Action key dispatched when the user changes the selection; `null` if read-only.
 * @see KEnumNode
 */
@Serializable
data class KEnumProps(
    val id: String = "",
    val enumName: String = "",
    val values: List<String> = emptyList(),
    val selectedValue: String = "",
    val onSelectionChange: JsonElement? = null
)

/**
 * Property bag for a data-bound list node.
 *
 * Tells the renderer which data source to iterate and what alias to use
 * for item-level template variables.
 *
 * ## JSON Wire Format
 * ```json
 * { "dataSource": "user.transactions", "itemAlias": "item" }
 * ```
 *
 * @property dataSource  Registry key prefix for the list (e.g., `"user.transactions"`).
 *                       Items are expected at `"$dataSource.0"`, `"$dataSource.1"`, etc.
 *                       Count is at `"$dataSource.count"`.
 * @property itemAlias   Template alias for the current item (e.g., `"item"` → `{{data:item:title}}`).
 * @see KDataListNode
 */
@Serializable
data class KDataListProps(
    val dataSource: String = "",
    val itemAlias: String = "item"
)
