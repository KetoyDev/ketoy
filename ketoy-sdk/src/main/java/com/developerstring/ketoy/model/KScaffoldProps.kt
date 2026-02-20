package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Scaffold & navigation component properties
// ─────────────────────────────────────────────────────────────

/**
 * Properties for a Material 3 `Scaffold` layout, the primary structural container
 * in a Jetpack Compose screen.
 *
 * This class holds the configuration slots and styling for a Ketoy scaffold,
 * including its top bar, bottom bar, snackbar host, and floating action button.
 * Each slot accepts a list of [KNode] children that are rendered into the
 * corresponding Compose `Scaffold` parameter.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "type": "Scaffold",
 *   "props": {
 *     "containerColor": "#FFFFFF",
 *     "floatingActionButtonPosition": "End"
 *   },
 *   "children": [...]
 * }
 * ```
 *
 * @property modifier                     Optional [KModifier] applied to the scaffold root.
 * @property topBar                        Child nodes rendered in the top-bar slot (typically a [KTopAppBarProps]-based node).
 * @property bottomBar                     Child nodes rendered in the bottom-bar slot (e.g. [KBottomAppBarProps] or [KNavigationBarProps]).
 * @property snackbarHost                  Child nodes rendered in the snackbar-host slot.
 * @property floatingActionButton          Child nodes rendered as the floating action button.
 * @property floatingActionButtonPosition  Placement of the FAB — typically `"End"` or `"Center"`.
 * @property containerColor                Background colour of the scaffold surface (CSS/hex string).
 * @property contentColor                  Default content colour used by scaffold children (CSS/hex string).
 * @property contentWindowInsets           Custom window insets applied to the scaffold content area.
 * @see KScaffoldNode
 * @see KTopAppBarProps
 * @see KBottomAppBarProps
 * @see KNavigationBarProps
 * @see KFloatingActionButtonProps
 */
@Serializable
data class KScaffoldProps(
    val modifier: KModifier? = null,
    val topBar: List<KNode>? = null,
    val bottomBar: List<KNode>? = null,
    val snackbarHost: List<KNode>? = null,
    val floatingActionButton: List<KNode>? = null,
    val floatingActionButtonPosition: String? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val contentWindowInsets: KWindowInsets? = null
)

/**
 * Properties for a Material 3 `TopAppBar` (or `MediumTopAppBar` / `LargeTopAppBar`).
 *
 * Configures the title, navigation icon, action buttons, colour palette, and
 * scroll behaviour for a top app bar. The [type] field selects between
 * small, medium, and large variants.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "type": "TopAppBar",
 *   "props": {
 *     "type": "Medium",
 *     "title": [{ "type": "Text", "props": { "text": "Home" } }]
 *   }
 * }
 * ```
 *
 * @property modifier        Optional [KModifier] applied to the app bar.
 * @property title           Child nodes rendered as the app bar title.
 * @property navigationIcon  Child nodes rendered as the leading navigation icon (e.g. a back arrow).
 * @property actions         Child nodes rendered in the trailing action area (e.g. [KAppBarActionProps]).
 * @property windowInsets    Custom window insets for the app bar.
 * @property colors          Colour configuration via [KTopAppBarColors].
 * @property scrollBehavior  Scroll behaviour configuration via [KTopAppBarScrollBehavior].
 * @property type            App bar variant — `"Small"`, `"Medium"`, or `"Large"`.
 * @property expandedHeight  Maximum height (in dp) when the app bar is expanded (medium/large variants).
 * @see KTopAppBarNode
 * @see KTopAppBarColors
 * @see KTopAppBarScrollBehavior
 * @see KAppBarActionProps
 */
@Serializable
data class KTopAppBarProps(
    val modifier: KModifier? = null,
    val title: List<KNode>? = null,
    val navigationIcon: List<KNode>? = null,
    val actions: List<KNode>? = null,
    val windowInsets: KWindowInsets? = null,
    val colors: KTopAppBarColors? = null,
    val scrollBehavior: KTopAppBarScrollBehavior? = null,
    val type: String? = null,
    val expandedHeight: Int? = null
)

/**
 * Properties for a Material 3 `BottomAppBar`.
 *
 * Configures the colour, elevation, padding, and optional scroll behaviour for
 * a bottom app bar. The bottom app bar is typically placed in the
 * [KScaffoldProps.bottomBar] slot and may contain icons, text, or a
 * secondary navigation menu.
 *
 * @property modifier        Optional [KModifier] applied to the bottom app bar.
 * @property containerColor  Background colour of the bar (CSS/hex string).
 * @property contentColor    Default content colour for children (CSS/hex string).
 * @property tonalElevation  Tonal elevation in dp that tints the surface colour.
 * @property contentPadding  Padding applied around the bar's content.
 * @property windowInsets    Custom window insets for the bottom app bar.
 * @property scrollBehavior  Scroll behaviour configuration via [KBottomAppBarScrollBehavior].
 * @see KBottomAppBarNode
 * @see KBottomAppBarScrollBehavior
 * @see KScaffoldProps
 */
@Serializable
data class KBottomAppBarProps(
    val modifier: KModifier? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val tonalElevation: Int? = null,
    val contentPadding: KPadding? = null,
    val windowInsets: KWindowInsets? = null,
    val scrollBehavior: KBottomAppBarScrollBehavior? = null
)

/**
 * Properties for a Material 3 `NavigationBar` (bottom navigation).
 *
 * Defines the appearance of the navigation bar container, which hosts
 * [KNavigationBarItemProps] children for tab-style switching. Commonly placed
 * in the [KScaffoldProps.bottomBar] slot for phone layouts.
 *
 * @property modifier        Optional [KModifier] applied to the navigation bar.
 * @property containerColor  Background colour of the bar (CSS/hex string).
 * @property contentColor    Default content colour for bar items (CSS/hex string).
 * @property tonalElevation  Tonal elevation in dp that tints the surface colour.
 * @property windowInsets    Custom window insets for the navigation bar.
 * @see KNavigationBarNode
 * @see KNavigationBarItemProps
 * @see KScaffoldProps
 */
@Serializable
data class KNavigationBarProps(
    val modifier: KModifier? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val tonalElevation: Int? = null,
    val windowInsets: KWindowInsets? = null
)

/**
 * Properties for a Material 3 `FloatingActionButton`.
 *
 * Configures the FAB's colour, shape, elevation, and interaction handling.
 * Place the resulting node in the [KScaffoldProps.floatingActionButton] slot.
 * The [type] field selects between standard, small, large, and extended variants.
 *
 * @property modifier          Optional [KModifier] applied to the FAB.
 * @property onClick           Action identifier dispatched when the user taps the FAB.
 * @property shape             Shape of the FAB — e.g. `"Circle"`, `"RoundedCorner"`.
 * @property containerColor    Background colour of the FAB (CSS/hex string).
 * @property contentColor      Icon/text colour inside the FAB (CSS/hex string).
 * @property elevation         State-dependent elevation via [KFloatingActionButtonElevation].
 * @property interactionSource Custom interaction source for ripple / focus effects.
 * @property type              FAB variant — `"Standard"`, `"Small"`, `"Large"`, or `"Extended"`.
 * @see KFloatingActionButtonNode
 * @see KFloatingActionButtonElevation
 * @see KScaffoldProps
 */
@Serializable
data class KFloatingActionButtonProps(
    val modifier: KModifier? = null,
    val onClick: String? = null,
    val shape: String? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val elevation: KFloatingActionButtonElevation? = null,
    val interactionSource: KInteractionSource? = null,
    val type: String? = null
)

/**
 * Properties for a Material 3 `Snackbar`.
 *
 * Holds the message, optional action / dismiss buttons, colours, shape, and
 * duration for a snackbar notification. Typically rendered inside a
 * [KSnackBarHostProps]-based host within a scaffold.
 *
 * @property modifier                  Optional [KModifier] applied to the snackbar.
 * @property action                    Child nodes rendered as the snackbar's action button.
 * @property dismissAction             Child nodes rendered as the dismiss action (e.g. a close icon).
 * @property actionOnNewLine           Whether the action button is placed on a separate line.
 * @property shape                     Corner shape of the snackbar — e.g. `"RoundedCorner"`.
 * @property containerColor            Background colour of the snackbar (CSS/hex string).
 * @property contentColor              Colour for the message text (CSS/hex string).
 * @property actionContentColor        Colour for the action button content (CSS/hex string).
 * @property dismissActionContentColor Colour for the dismiss action content (CSS/hex string).
 * @property message                   The text message displayed in the snackbar.
 * @property duration                  Display duration — `"Short"`, `"Long"`, or `"Indefinite"`.
 * @see KSnackBarNode
 * @see KSnackBarHostProps
 */
@Serializable
data class KSnackBarProps(
    val modifier: KModifier? = null,
    val action: List<KNode>? = null,
    val dismissAction: List<KNode>? = null,
    val actionOnNewLine: Boolean? = null,
    val shape: String? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val actionContentColor: String? = null,
    val dismissActionContentColor: String? = null,
    val message: String? = null,
    val duration: String? = null
)

/**
 * Properties for a Material 3 `SnackbarHost`.
 *
 * The snackbar host manages the display queue of [KSnackBarProps]-based
 * snackbar messages. It is placed in the [KScaffoldProps.snackbarHost] slot.
 *
 * @property hostState  Reference key identifying the snackbar host state manager.
 * @property modifier   Optional [KModifier] applied to the snackbar host.
 * @property snackbar   Child nodes providing a custom snackbar layout.
 * @see KSnackBarHostNode
 * @see KSnackBarProps
 * @see KScaffoldProps
 */
@Serializable
data class KSnackBarHostProps(
    val hostState: String? = null,
    val modifier: KModifier? = null,
    val snackbar: List<KNode>? = null
)

/**
 * Properties for a Material 3 `NavigationDrawerItem`.
 *
 * Represents a single selectable entry inside a navigation drawer.
 * Each item can display an icon, label, optional badge, and supports custom
 * colours for both selected and unselected states.
 *
 * @property selected  Whether this drawer item is currently selected.
 * @property onClick   Action identifier dispatched when the user taps the item.
 * @property icon      Child nodes rendered as the leading icon.
 * @property modifier  Optional [KModifier] applied to the drawer item.
 * @property enabled   Whether the item is interactive; disabled items are greyed out.
 * @property label     Child nodes rendered as the item text label.
 * @property badge     Child nodes rendered as a trailing badge (e.g. notification count).
 * @property colors    Colour overrides for selected/unselected states via [KNavigationDrawerItemColors].
 * @property shape     Shape of the item container — e.g. `"RoundedCorner"`.
 * @see KNavigationDrawerItemNode
 * @see KNavigationDrawerItemColors
 */
@Serializable
data class KNavigationDrawerItemProps(
    val selected: Boolean? = null,
    val onClick: String? = null,
    val icon: List<KNode>? = null,
    val modifier: KModifier? = null,
    val enabled: Boolean? = null,
    val label: List<KNode>? = null,
    val badge: List<KNode>? = null,
    val colors: KNavigationDrawerItemColors? = null,
    val shape: String? = null
)

/**
 * Properties for a custom navigation item with full composable flexibility.
 *
 * Unlike the standard Material navigation item, this variant exposes
 * individual colour properties for container, content, indicator, and ripple
 * in both selected and unselected states. Use it when the built-in
 * `NavigationBarItem` styling is insufficient.
 *
 * @property selected               Whether this item is currently selected.
 * @property onClick                Action identifier dispatched on tap.
 * @property icon                   Child nodes rendered as the default (unselected) icon.
 * @property selectedIcon           Child nodes rendered as the icon when selected.
 * @property modifier               Optional [KModifier] applied to the item.
 * @property enabled                Whether the item is interactive.
 * @property label                  Child nodes rendered as the item text label.
 * @property alwaysShowLabel        Whether the label is visible even when the item is not selected.
 * @property containerColor         Background colour in the unselected state (CSS/hex string).
 * @property contentColor           Content colour in the unselected state (CSS/hex string).
 * @property selectedContainerColor Background colour in the selected state (CSS/hex string).
 * @property selectedContentColor   Content colour in the selected state (CSS/hex string).
 * @property indicatorColor         Colour of the selection indicator pill (CSS/hex string).
 * @property rippleColor            Colour of the touch ripple effect (CSS/hex string).
 * @see KCustomNavigationItemNode
 * @see KNavigationBarProps
 */
@Serializable
data class KCustomNavigationItemProps(
    val selected: Boolean? = null,
    val onClick: String? = null,
    val icon: List<KNode>? = null,
    val selectedIcon: List<KNode>? = null,
    val modifier: KModifier? = null,
    val enabled: Boolean? = null,
    val label: List<KNode>? = null,
    val alwaysShowLabel: Boolean? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val selectedContainerColor: String? = null,
    val selectedContentColor: String? = null,
    val indicatorColor: String? = null,
    val rippleColor: String? = null
)

/**
 * Properties for a Material 3 `NavigationRail`.
 *
 * A navigation rail is a vertical navigation strip intended for tablet and
 * desktop layouts. It hosts [KNavigationRailItemProps] children and may
 * include an optional header (e.g. a FAB or logo).
 *
 * @property modifier        Optional [KModifier] applied to the rail.
 * @property containerColor  Background colour of the rail (CSS/hex string).
 * @property contentColor    Default content colour for rail items (CSS/hex string).
 * @property header          Child nodes rendered at the top of the rail (e.g. a FAB).
 * @property windowInsets    Custom window insets for the navigation rail.
 * @see KNavigationRailNode
 * @see KNavigationRailItemProps
 */
@Serializable
data class KNavigationRailProps(
    val modifier: KModifier? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val header: List<KNode>? = null,
    val windowInsets: KWindowInsets? = null
)

/**
 * Properties for a single item inside a [KNavigationRailProps]-based rail.
 *
 * Each rail item displays an icon with an optional label and supports distinct
 * icons for the selected and unselected states.
 *
 * @property selected        Whether this rail item is currently selected.
 * @property onClick         Action identifier dispatched on tap.
 * @property icon            Child nodes rendered as the default (unselected) icon.
 * @property selectedIcon    Child nodes rendered as the icon when selected.
 * @property modifier        Optional [KModifier] applied to the rail item.
 * @property enabled         Whether the item is interactive.
 * @property label           Child nodes rendered as the item text label.
 * @property alwaysShowLabel Whether the label is visible even when the item is not selected.
 * @see KNavigationRailItemNode
 * @see KNavigationRailProps
 */
@Serializable
data class KNavigationRailItemProps(
    val selected: Boolean = false,
    val onClick: String? = null,
    val icon: List<KNode>? = null,
    val selectedIcon: List<KNode>? = null,
    val modifier: KModifier? = null,
    val enabled: Boolean? = null,
    val label: List<KNode>? = null,
    val alwaysShowLabel: Boolean? = null
)

/**
 * Properties for an action button displayed inside a [KTopAppBarProps]-based app bar.
 *
 * Each action is typically rendered as a Material 3 `IconButton` in the
 * trailing action area of the top app bar.
 *
 * @property onClick           Action identifier dispatched on tap.
 * @property modifier          Optional [KModifier] applied to the action icon button.
 * @property enabled           Whether the action button is interactive.
 * @property colors            Colour overrides via [KIconButtonColors].
 * @property interactionSource Custom interaction source for ripple / focus effects.
 * @see KAppBarActionNode
 * @see KTopAppBarProps
 * @see KIconButtonColors
 */
@Serializable
data class KAppBarActionProps(
    val onClick: String? = null,
    val modifier: KModifier? = null,
    val enabled: Boolean? = null,
    val colors: KIconButtonColors? = null,
    val interactionSource: KInteractionSource? = null
)

// ── Supporting data classes ─────────────────────────────────

/**
 * Represents custom window insets in density-independent pixels (dp).
 *
 * Maps to Jetpack Compose `WindowInsets` and allows the server to define
 * per-edge padding for system bars, keyboard, or custom regions.
 * The optional [type] field can select a named inset set such as
 * `"systemBars"`, `"navigationBars"`, or `"ime"`.
 *
 * @property left   Left inset in dp.
 * @property top    Top inset in dp.
 * @property right  Right inset in dp.
 * @property bottom Bottom inset in dp.
 * @property type   Named inset preset — e.g. `"systemBars"`, `"navigationBars"`, `"ime"`.
 * @see KScaffoldProps
 * @see KTopAppBarProps
 * @see KBottomAppBarProps
 */
@Serializable
data class KWindowInsets(
    val left: Int? = null,
    val top: Int? = null,
    val right: Int? = null,
    val bottom: Int? = null,
    val type: String? = null
)

/**
 * Colour configuration for a Material 3 `TopAppBar`.
 *
 * Allows granular control over the app bar's container colour at rest and
 * when scrolled, as well as the content colours for the navigation icon,
 * title, and action icons.
 *
 * @property containerColor               Background colour at rest (CSS/hex string).
 * @property scrolledContainerColor        Background colour when content is scrolled behind the bar (CSS/hex string).
 * @property navigationIconContentColor    Colour of the leading navigation icon (CSS/hex string).
 * @property titleContentColor             Colour of the title text (CSS/hex string).
 * @property actionIconContentColor        Colour of trailing action icons (CSS/hex string).
 * @see KTopAppBarProps
 */
@Serializable
data class KTopAppBarColors(
    val containerColor: String? = null,
    val scrolledContainerColor: String? = null,
    val navigationIconContentColor: String? = null,
    val titleContentColor: String? = null,
    val actionIconContentColor: String? = null
)

/**
 * Scroll behaviour configuration for a Material 3 `TopAppBar`.
 *
 * Controls how the top app bar reacts to nested scroll events —
 * for example, collapsing on scroll or pinning in place.
 *
 * @property type      Behaviour type — `"Pinned"`, `"EnterAlways"`, or `"ExitUntilCollapsed"`.
 * @property canScroll Whether scrolling is enabled for the app bar.
 * @see KTopAppBarProps
 */
@Serializable
data class KTopAppBarScrollBehavior(
    val type: String? = null,
    val canScroll: Boolean? = null
)

/**
 * Scroll behaviour configuration for a Material 3 `BottomAppBar`.
 *
 * Controls whether the bottom app bar hides or stays visible during
 * nested scroll events.
 *
 * @property type      Behaviour type — e.g. `"ExitAlways"`.
 * @property canScroll Whether scrolling is enabled for the bottom app bar.
 * @see KBottomAppBarProps
 */
@Serializable
data class KBottomAppBarScrollBehavior(
    val type: String? = null,
    val canScroll: Boolean? = null
)

/**
 * State-dependent elevation values for a `FloatingActionButton`.
 *
 * Maps to `FloatingActionButtonDefaults.elevation()` in Compose, providing
 * distinct shadow depths for each interaction state.
 *
 * @property defaultElevation  Elevation in dp when the FAB is at rest.
 * @property pressedElevation  Elevation in dp when the FAB is pressed.
 * @property focusedElevation  Elevation in dp when the FAB is focused.
 * @property hoveredElevation  Elevation in dp when the FAB is hovered.
 * @property disabledElevation Elevation in dp when the FAB is disabled.
 * @see KFloatingActionButtonProps
 */
@Serializable
data class KFloatingActionButtonElevation(
    val defaultElevation: Int? = null,
    val pressedElevation: Int? = null,
    val focusedElevation: Int? = null,
    val hoveredElevation: Int? = null,
    val disabledElevation: Int? = null
)

/**
 * Colour overrides for a `NavigationDrawerItem` in selected and unselected states.
 *
 * Provides granular control over the container, icon, text, and badge colours
 * for both the active (selected) and inactive (unselected) states of a
 * navigation drawer entry.
 *
 * @property selectedContainerColor    Background colour when selected (CSS/hex string).
 * @property unselectedContainerColor  Background colour when not selected (CSS/hex string).
 * @property selectedIconColor         Icon colour when selected (CSS/hex string).
 * @property unselectedIconColor       Icon colour when not selected (CSS/hex string).
 * @property selectedTextColor         Label text colour when selected (CSS/hex string).
 * @property unselectedTextColor       Label text colour when not selected (CSS/hex string).
 * @property selectedBadgeColor        Badge colour when selected (CSS/hex string).
 * @property unselectedBadgeColor      Badge colour when not selected (CSS/hex string).
 * @see KNavigationDrawerItemProps
 */
@Serializable
data class KNavigationDrawerItemColors(
    val selectedContainerColor: String? = null,
    val unselectedContainerColor: String? = null,
    val selectedIconColor: String? = null,
    val unselectedIconColor: String? = null,
    val selectedTextColor: String? = null,
    val unselectedTextColor: String? = null,
    val selectedBadgeColor: String? = null,
    val unselectedBadgeColor: String? = null
)

/**
 * Colour overrides for a Material 3 `IconButton`.
 *
 * Controls the container and content colours for both the enabled and
 * disabled states of an icon button, such as those used in [KAppBarActionProps].
 *
 * @property containerColor         Background colour when enabled (CSS/hex string).
 * @property contentColor           Icon colour when enabled (CSS/hex string).
 * @property disabledContainerColor Background colour when disabled (CSS/hex string).
 * @property disabledContentColor   Icon colour when disabled (CSS/hex string).
 * @see KAppBarActionProps
 */
@Serializable
data class KIconButtonColors(
    val containerColor: String? = null,
    val contentColor: String? = null,
    val disabledContainerColor: String? = null,
    val disabledContentColor: String? = null
)

// ─────────────────────────────────────────────────────────────
//  NavigationBarItem properties
// ─────────────────────────────────────────────────────────────

/**
 * Properties for a single item inside a Material 3 `NavigationBar`.
 *
 * Each navigation bar item displays an icon with an optional label and
 * supports distinct icons for selected and unselected states. Colour
 * overrides are available via [KNavigationBarItemColors].
 *
 * @property selected      Whether this navigation item is currently selected.
 * @property onClick       Action identifier dispatched when the user taps the item.
 * @property modifier      Optional [KModifier] applied to the item.
 * @property enabled       Whether the item is interactive.
 * @property alwaysShowLabel Whether the label is visible even when the item is not selected.
 * @property icon          Child nodes rendered as the default (unselected) icon.
 * @property selectedIcon  Child nodes rendered as the icon when selected.
 * @property label         Child nodes rendered as the item text label.
 * @property colors        Colour overrides via [KNavigationBarItemColors].
 * @see KNavigationBarItemNode
 * @see KNavigationBarProps
 * @see KNavigationBarItemColors
 */
@Serializable
data class KNavigationBarItemProps(
    val selected: Boolean = false,
    val onClick: String? = null,
    val modifier: KModifier? = null,
    val enabled: Boolean? = null,
    val alwaysShowLabel: Boolean? = null,
    val icon: List<KNode>? = null,
    val selectedIcon: List<KNode>? = null,
    val label: List<KNode>? = null,
    val colors: KNavigationBarItemColors? = null
)

/**
 * Colour overrides for a `NavigationBarItem` across its visual states.
 *
 * Provides per-state colour control for the icon, text, and selection
 * indicator of a navigation bar entry.
 *
 * @property selectedIconColor   Icon colour when selected (CSS/hex string).
 * @property selectedTextColor   Label colour when selected (CSS/hex string).
 * @property indicatorColor      Colour of the selection indicator pill (CSS/hex string).
 * @property unselectedIconColor Icon colour when not selected (CSS/hex string).
 * @property unselectedTextColor Label colour when not selected (CSS/hex string).
 * @property disabledIconColor   Icon colour when the item is disabled (CSS/hex string).
 * @property disabledTextColor   Label colour when the item is disabled (CSS/hex string).
 * @see KNavigationBarItemProps
 */
@Serializable
data class KNavigationBarItemColors(
    val selectedIconColor: String? = null,
    val selectedTextColor: String? = null,
    val indicatorColor: String? = null,
    val unselectedIconColor: String? = null,
    val unselectedTextColor: String? = null,
    val disabledIconColor: String? = null,
    val disabledTextColor: String? = null
)

// ─────────────────────────────────────────────────────────────
//  ModalBottomSheet properties
// ─────────────────────────────────────────────────────────────

/**
 * Properties for a Material 3 `ModalBottomSheet`.
 *
 * Configures the appearance and behaviour of a bottom sheet that slides up
 * as a modal overlay. Supports customisation of the scrim, drag handle,
 * shape, colours, and dismiss callback.
 *
 * ## JSON Wire Format
 * ```json
 * {
 *   "type": "ModalBottomSheet",
 *   "props": {
 *     "containerColor": "#FFFFFF",
 *     "tonalElevation": 4
 *   },
 *   "children": [...]
 * }
 * ```
 *
 * @property modifier          Optional [KModifier] applied to the bottom sheet surface.
 * @property onDismissRequest  Action identifier dispatched when the user dismisses the sheet (e.g. taps the scrim).
 * @property sheetState        Initial visibility state of the sheet — e.g. `"Expanded"`, `"PartiallyExpanded"`, `"Hidden"`.
 * @property shape             Corner shape of the sheet — e.g. `"RoundedCorner"`.
 * @property containerColor    Background colour of the sheet surface (CSS/hex string).
 * @property contentColor      Default content colour inside the sheet (CSS/hex string).
 * @property tonalElevation    Tonal elevation in dp that tints the surface colour.
 * @property scrimColor        Colour of the background scrim overlay (CSS/hex string).
 * @property dragHandle        Child nodes rendered as the sheet's drag handle indicator.
 * @see KModalBottomSheetNode
 * @see KScaffoldProps
 */
@Serializable
data class KModalBottomSheetProps(
    val modifier: KModifier? = null,
    val onDismissRequest: String? = null,
    val sheetState: String? = null,
    val shape: String? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val tonalElevation: Int? = null,
    val scrimColor: String? = null,
    val dragHandle: List<KNode>? = null
)
