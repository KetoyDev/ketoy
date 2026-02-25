package com.developerstring.ketoy.model

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Root of the Ketoy Server-Driven UI node hierarchy.
 *
 * Every visual element in a Ketoy screen is represented as a subtype of [KNode].
 * The hierarchy is **serializable** via `kotlinx.serialization` and uses a JSON
 * discriminator field `"type"` to identify the concrete node type during
 * deserialization.
 *
 * ## How it works
 * A screen's JSON tree is parsed into a [KNode] tree, which the
 * [KetoyRenderer][com.developerstring.ketoy.renderer.KetoyRenderer] walks to
 * emit native Jetpack Compose UI. You can also build trees programmatically via
 * the Ketoy DSL (e.g. [KColumn][com.developerstring.ketoy.dsl.TopLevelBuilders]).
 *
 * ## Example — JSON to KNode
 * ```json
 * {
 *   "type": "Column",
 *   "children": [
 *     { "type": "Text", "props": { "text": "Hello" } }
 *   ]
 * }
 * ```
 *
 * ## Example — DSL to KNode
 * ```kotlin
 * val tree: KNode = KColumn {
 *     KText("Hello, Ketoy!")
 * }
 * ```
 *
 * @see KColumnNode
 * @see KRowNode
 * @see KBoxNode
 * @see KTextNode
 * @see KButtonNode
 * @see KScaffoldNode
 * @see KComponentNode
 * @see com.developerstring.ketoy.renderer.KetoyRenderer
 * @see com.developerstring.ketoy.core.KetoyJsonUtils
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
sealed class KNode

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  Layout Containers
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * A vertical layout container, analogous to Jetpack Compose `Column`.
 *
 * Arranges [children] vertically with optional spacing, alignment, and modifiers.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Column", "props": { "verticalArrangement": "SpaceBetween" }, "children": [...] }
 * ```
 *
 * @property props  Layout configuration including [KModifier], arrangement and alignment.
 * @property children  The ordered list of child [KNode]s rendered top-to-bottom.
 * @see KRowNode
 * @see KBoxNode
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable @SerialName("Column")
data class KColumnNode(
    val props: KColumnProps = KColumnProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A horizontal layout container, analogous to Jetpack Compose `Row`.
 *
 * Arranges [children] horizontally with optional spacing, alignment, and modifiers.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Row", "props": { "horizontalArrangement": "SpaceEvenly" }, "children": [...] }
 * ```
 *
 * @property props  Layout configuration including [KModifier], arrangement and alignment.
 * @property children  The ordered list of child [KNode]s rendered start-to-end.
 * @see KColumnNode
 * @see KBoxNode
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable @SerialName("Row")
data class KRowNode(
    val props: KRowProps = KRowProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A stacking layout container, analogous to Jetpack Compose `Box`.
 *
 * Overlays [children] on top of each other with optional content alignment.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Box", "props": { "contentAlignment": "Center" }, "children": [...] }
 * ```
 *
 * @property props  Layout configuration including [KModifier] and content alignment.
 * @property children  The list of child [KNode]s stacked in z-order.
 * @see KColumnNode
 * @see KRowNode
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable @SerialName("Box")
data class KBoxNode(
    val props: KBoxProps = KBoxProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A vertically scrolling lazy list, analogous to Jetpack Compose `LazyColumn`.
 *
 * Renders [children] on-demand as the user scrolls, providing efficient
 * performance for large data sets.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "LazyColumn", "props": { "verticalArrangement": "spacedBy:8" }, "children": [...] }
 * ```
 *
 * @property props  Configuration including arrangement, alignment, content padding, and scroll settings.
 * @property children  The items to lazily render.
 * @see KLazyRowNode
 * @see com.developerstring.ketoy.dsl.KLazyListScope
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable @SerialName("LazyColumn")
data class KLazyColumnNode(
    val props: KLazyColumnProps = KLazyColumnProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A horizontally scrolling lazy list, analogous to Jetpack Compose `LazyRow`.
 *
 * Renders [children] on-demand as the user scrolls horizontally. Ideal for
 * carousels, chip rows, or horizontal card lists.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "LazyRow", "props": { "horizontalArrangement": "spacedBy:12" }, "children": [...] }
 * ```
 *
 * @property props  Configuration including arrangement, alignment, content padding, and scroll settings.
 * @property children  The items to lazily render.
 * @see KLazyColumnNode
 * @see com.developerstring.ketoy.dsl.KLazyListScope
 * @see com.developerstring.ketoy.renderer.LayoutRenderer
 */
@Serializable @SerialName("LazyRow")
data class KLazyRowNode(
    val props: KLazyRowProps = KLazyRowProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A clickable button container, analogous to Jetpack Compose `Button`.
 *
 * Wraps [children] (typically a [KTextNode]) and triggers an `onClick` action
 * when tapped. The action can be a registered callback ID, a navigation action,
 * or a function-call action resolved at render time.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "type": "Button",
 *   "props": { "containerColor": "#6200EE", "onClick": "my_action_id" },
 *   "children": [{ "type": "Text", "props": { "text": "Tap Me" } }]
 * }
 * ```
 *
 * @property props  Button styling and the onClick handler reference.
 * @property children  The content displayed inside the button.
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 * @see com.developerstring.ketoy.renderer.OnClickResolver
 */
@Serializable @SerialName("Button")
data class KButtonNode(
    val props: KButtonProps = KButtonProps(),
    val children: List<KNode> = emptyList()
) : KNode()

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  Leaf Widgets
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * A text display node, analogous to Jetpack Compose `Text`.
 *
 * Renders a styled label with configurable font size, weight, color,
 * alignment, max lines, and overflow behaviour.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Text", "props": { "text": "Hello", "fontSize": 24, "fontWeight": "Bold" } }
 * ```
 *
 * @property props  All text-styling and content properties.
 * @see KTextProps
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable @SerialName("Text")
data class KTextNode(val props: KTextProps = KTextProps()) : KNode()

/**
 * An empty spacing node, analogous to Jetpack Compose `Spacer`.
 *
 * Inserts blank space with a fixed width and/or height between sibling nodes.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Spacer", "props": { "height": 16 } }
 * ```
 *
 * @property props  Width and height for the spacer.
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable @SerialName("Spacer")
data class KSpacerNode(val props: KSpacerProps = KSpacerProps()) : KNode()

/**
 * A Material 3 card container, analogous to Jetpack Compose `Card`.
 *
 * Groups [children] inside a raised or outlined surface with optional
 * shape, elevation, colours, and click behaviour.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Card", "props": { "elevation": 4, "shape": "rounded:12" }, "children": [...] }
 * ```
 *
 * @property props  Card styling including shape, colours, elevation, and optional onClick.
 * @property children  The content nodes displayed inside the card.
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable @SerialName("Card")
data class KCardNode(
    val props: KCardProps = KCardProps(),
    val children: MutableList<KNode> = mutableListOf()
) : KNode()

/**
 * A text input field, analogous to Jetpack Compose `TextField` / `OutlinedTextField`.
 *
 * Supports label, placeholder, leading/trailing icons, keyboard options,
 * visual transformations (e.g. password masking), and value-change callbacks.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "TextField", "props": { "label": "Email", "keyboardType": "Email" } }
 * ```
 *
 * @property props  Full text-field configuration.
 * @see KTextFieldProps
 * @see com.developerstring.ketoy.renderer.TextFieldRenderer
 */
@Serializable @SerialName("TextField")
data class KTextFieldNode(val props: KTextFieldProps = KTextFieldProps()) : KNode()

/**
 * An image display node supporting network URLs, local resources, and asset paths.
 *
 * Uses [Coil](https://coil-kt.github.io/coil/) for async image loading when the
 * source is a URL.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Image", "props": { "source": { "type": "url", "value": "https://..." } } }
 * ```
 *
 * @property props  Image source, scale type, content description, and modifier.
 * @see KImageSource
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable @SerialName("Image")
data class KImageNode(val props: KImageProps = KImageProps()) : KNode()

/**
 * A Material icon node using the Material Icons font family.
 *
 * Resolves icon names from the Material Icons set (Filled, Outlined, Rounded, Sharp, TwoTone).
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Icon", "props": { "icon": "Home", "style": "Outlined", "size": 24 } }
 * ```
 *
 * @property props  Icon name, style variant, size, colour, and accessibility label.
 * @see com.developerstring.ketoy.util.KIcons
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable @SerialName("Icon")
data class KIconNode(val props: KIconProps = KIconProps()) : KNode()

/**
 * An icon wrapped in a clickable button, analogous to Jetpack Compose `IconButton`.
 *
 * Combines an icon with an onClick handler. Supports container/content colours
 * and disabled states.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "IconButton", "props": { "icon": "Settings", "onClick": "open_settings" } }
 * ```
 *
 * @property props  Icon, onClick handler, colours, and enabled state.
 * @property children  Optional child nodes (rarely used; the icon is rendered from props).
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable @SerialName("IconButton")
data class KIconButtonNode(
    val props: KIconButtonProps = KIconButtonProps(),
    val children: List<KNode> = emptyList()
) : KNode()

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  Custom Component
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * A dynamically-resolved custom component registered via [KComponentRegistry][com.developerstring.ketoy.registry.KComponentRegistry].
 *
 * Allows developers to register their own Jetpack Compose composables and
 * reference them by name from JSON. This enables extending Ketoy's built-in
 * widget set with app-specific components.
 *
 * ## JSON Wire Format
 * ```json
 * { "type": "Component", "props": { "name": "MyRatingBar", "properties": { "rating": 4.5 } } }
 * ```
 *
 * ## Registration
 * ```kotlin
 * KComponentRegistry.register("MyRatingBar") { props, children ->
 *     MyRatingBarComposable(rating = props["rating"] as? Float ?: 0f)
 * }
 * ```
 *
 * @property props  Component name, custom properties map, modifier, and fallback config.
 * @property children  Optional child nodes passed to the custom composable.
 * @property metadata  Transient metadata (not serialized) providing import and version info.
 * @see com.developerstring.ketoy.registry.KComponentRegistry
 * @see com.developerstring.ketoy.renderer.ComponentRenderer
 */
@Serializable @SerialName("Component")
data class KComponentNode(
    val props: KComponentProps = KComponentProps(),
    val children: List<KNode> = emptyList(),
    @Transient val metadata: KComponentMetadata? = null
) : KNode()

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  Scaffold Components
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * A Material 3 Scaffold layout, analogous to Jetpack Compose `Scaffold`.
 *
 * Provides slots for a top app bar, bottom bar, floating action button,
 * snackbar host, and main body content — all configurable from JSON.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "type": "Scaffold",
 *   "props": { "containerColor": "#FFFFFF" },
 *   "children": [
 *     { "type": "TopAppBar", "props": { "title": "Home" } },
 *     { "type": "Column", "children": [...] }
 *   ]
 * }
 * ```
 *
 * @property props  Scaffold colours and configuration.
 * @property children  Scaffold slot components (TopAppBar, BottomAppBar, NavigationBar, FAB, body content).
 * @see KTopAppBarNode
 * @see KNavigationBarNode
 * @see KFloatingActionButtonNode
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("Scaffold")
data class KScaffoldNode(
    val props: KScaffoldProps = KScaffoldProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A Material 3 top app bar node.
 *
 * Renders a title, optional navigation icon, and action buttons at the top of a [KScaffoldNode].
 *
 * @property props  Title, colours, navigation icon, centre alignment, and scroll behaviour.
 * @property children  Action button nodes ([KAppBarActionNode]) or icon content.
 * @see KScaffoldNode
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("TopAppBar")
data class KTopAppBarNode(
    val props: KTopAppBarProps = KTopAppBarProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A Material 3 bottom app bar node.
 *
 * Renders content at the bottom of a [KScaffoldNode], typically containing
 * action icons or a secondary navigation area.
 *
 * @property props  Bottom app bar styling and configuration.
 * @property children  The content nodes inside the bottom app bar.
 * @see KScaffoldNode
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("BottomAppBar")
data class KBottomAppBarNode(
    val props: KBottomAppBarProps = KBottomAppBarProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A Material 3 bottom navigation bar with selectable items.
 *
 * Displays a row of [KNavigationBarItemNode]s at the bottom of a [KScaffoldNode].
 * Each item supports an icon, label, and selection state.
 *
 * @property props  Bar styling (colours, elevation).
 * @property children  [KNavigationBarItemNode] entries.
 * @see KNavigationBarItemNode
 * @see KScaffoldNode
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("NavigationBar")
data class KNavigationBarNode(
    val props: KNavigationBarProps = KNavigationBarProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A Floating Action Button (FAB) node.
 *
 * Renders a Material 3 FAB inside a [KScaffoldNode] with optional icon,
 * custom colours, and onClick handler.
 *
 * @property props  FAB styling, icon, and onClick reference.
 * @property children  Optional content nodes (typically an icon or text).
 * @see KScaffoldNode
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("FloatingActionButton")
data class KFloatingActionButtonNode(
    val props: KFloatingActionButtonProps = KFloatingActionButtonProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A Material 3 snackbar notification node.
 *
 * @property props  Snackbar message, action label, colours, and dismiss behaviour.
 * @property children  Optional custom content for the snackbar body.
 * @see KSnackBarHostNode
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("SnackBar")
data class KSnackBarNode(
    val props: KSnackBarProps = KSnackBarProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A snackbar host that manages displaying [KSnackBarNode] messages within a [KScaffoldNode].
 *
 * @property props  Host styling configuration.
 * @see KSnackBarNode
 * @see KScaffoldNode
 */
@Serializable @SerialName("SnackBarHost")
data class KSnackBarHostNode(val props: KSnackBarHostProps = KSnackBarHostProps()) : KNode()

/**
 * A navigation drawer item displayed inside a modal or permanent drawer.
 *
 * @property props  Label, icon, selected state, colours, and onClick handler.
 * @property children  Optional custom content for the drawer item.
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("NavigationDrawerItem")
data class KNavigationDrawerItemNode(
    val props: KNavigationDrawerItemProps = KNavigationDrawerItemProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A fully custom navigation item with flexible content, allowing composable
 * children instead of the standard icon + label pattern.
 *
 * @property props  Custom navigation item configuration.
 * @property children  Custom content nodes rendered for this item.
 * @see KNavigationBarNode
 */
@Serializable @SerialName("CustomNavigationItem")
data class KCustomNavigationItemNode(
    val props: KCustomNavigationItemProps = KCustomNavigationItemProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A Material 3 navigation rail for tablet / large-screen layouts.
 *
 * Displays a vertical strip of [KNavigationRailItemNode]s along the side
 * of the screen. Ideal for responsive layouts that switch from bottom bar
 * on phones to rail on tablets.
 *
 * @property props  Rail styling and configuration.
 * @property children  [KNavigationRailItemNode] entries.
 * @see KNavigationRailItemNode
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("NavigationRail")
data class KNavigationRailNode(
    val props: KNavigationRailProps = KNavigationRailProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A single item in a [KNavigationRailNode].
 *
 * @property props  Icon, label, selected state, and colours.
 * @property children  Optional custom content for the rail item.
 * @see KNavigationRailNode
 */
@Serializable @SerialName("NavigationRailItem")
data class KNavigationRailItemNode(
    val props: KNavigationRailItemProps = KNavigationRailItemProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * An action button displayed in a [KTopAppBarNode].
 *
 * Typically rendered as an [IconButton] in the app bar's action row.
 *
 * @property props  Icon, onClick handler, and styling.
 * @property children  Optional custom content.
 * @see KTopAppBarNode
 */
@Serializable @SerialName("AppBarAction")
data class KAppBarActionNode(
    val props: KAppBarActionProps = KAppBarActionProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A single item in a [KNavigationBarNode].
 *
 * Displays an icon, optional label, and handles selection state changes.
 *
 * @property props  Icon, label, selected state, colours, and onClick handler.
 * @property children  Optional custom content.
 * @see KNavigationBarNode
 */
@Serializable @SerialName("NavigationBarItem")
data class KNavigationBarItemNode(
    val props: KNavigationBarItemProps = KNavigationBarItemProps(),
    val children: List<KNode> = emptyList()
) : KNode()

/**
 * A Material 3 modal bottom sheet overlay.
 *
 * Slides up from the bottom to display [children] in a dismissable sheet.
 *
 * @property props  Sheet styling, drag handle visibility, and dismiss behaviour.
 * @property children  The content nodes displayed inside the sheet.
 * @see com.developerstring.ketoy.renderer.ScaffoldRenderer
 */
@Serializable @SerialName("ModalBottomSheet")
data class KModalBottomSheetNode(
    val props: KModalBottomSheetProps = KModalBottomSheetProps(),
    val children: List<KNode> = emptyList()
) : KNode()

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  Data Structures
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * A data-class node for binding structured data to the SDUI tree.
 *
 * Registers field values into [KetoyVariableRegistry][com.developerstring.ketoy.core.KetoyVariableRegistry]
 * so that template placeholders like `{{data:userId:name}}` are resolved at render time.
 *
 * @property props  The class name, unique ID, and field key-value map.
 * @see com.developerstring.ketoy.core.KetoyVariableRegistry
 */
@Serializable @SerialName("DataClass")
data class KDataClassNode(val props: KDataClassProps = KDataClassProps()) : KNode()

/**
 * An enum node representing a set of selectable values.
 *
 * Registers the enum into [KetoyVariableRegistry][com.developerstring.ketoy.core.KetoyVariableRegistry]
 * and supports selection change callbacks.
 *
 * @property props  Enum name, values list, selected value, and change handler.
 * @see com.developerstring.ketoy.core.KetoyVariableRegistry
 */
@Serializable @SerialName("Enum")
data class KEnumNode(val props: KEnumProps = KEnumProps()) : KNode()
