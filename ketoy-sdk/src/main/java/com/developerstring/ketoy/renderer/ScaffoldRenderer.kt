/**
 * Scaffold and navigation-component renderers for the Ketoy SDUI library.
 *
 * This file maps scaffold-family [UIComponent] nodes to their Material 3
 * Jetpack Compose counterparts, covering the full screen-chrome surface:
 *
 * - [RenderScaffold]              — top-level `Scaffold` with slot-based bars
 * - [RenderTopAppBar]             — small / centerAligned / medium / large top bar
 * - [RenderBottomAppBar]          — bottom toolbar
 * - [RenderNavigationBar]         — bottom navigation bar with items
 * - [RenderNavigationBarItem]     — single tab inside a [NavigationBar]
 * - [RenderNavigationDrawerItem]  — item inside a navigation drawer
 * - [RenderCustomNavigationItem]  — button-based fallback navigation item
 * - [RenderFloatingActionButton]  — regular / small / large / extended FAB
 * - [RenderSnackBar]              — standalone Snackbar
 * - [RenderSnackBarHost]          — SnackbarHost wrapper
 * - [RenderAppBarAction]          — IconButton wrapper for TopAppBar actions
 * - [RenderNavigationRail]        — side navigation rail
 * - [RenderNavigationRailItem]    — single item inside a NavigationRail
 * - [RenderModalBottomSheet]      — modal bottom sheet
 *
 * @see RenderComponent
 * @see UIComponent
 * @see OnClickResolver
 */
package com.developerstring.ketoy.renderer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.developerstring.ketoy.navigation.LocalKetoyNavController
import com.developerstring.ketoy.parser.*
import kotlinx.serialization.json.*
import androidx.compose.ui.platform.LocalContext

// ═══════════════════════════════════════════════════════════════════
//  Scaffold
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 `Scaffold` from a [UIComponent] node.
 *
 * The scaffold's chrome slots (`topBar`, `bottomBar`, `snackbarHost`,
 * `floatingActionButton`) are specified as JSON arrays within the props.
 * Each array element is decoded into a [UIComponent] and rendered via
 * [RenderComponent]. The body content comes from [UIComponent.children].
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 * | `containerColor` | string | `background` | Scaffold background colour |
 * | `contentColor` | string | `onBackground` | Default text/icon colour |
 * | `contentWindowInsets` | object | defaults | Window insets for content area |
 * | `topBar` | array | – | Top-bar slot components |
 * | `bottomBar` | array | – | Bottom-bar slot components |
 * | `snackbarHost` | array | – | Snackbar-host slot components |
 * | `floatingActionButton` | array | – | FAB slot components |
 * | `floatingActionButtonPosition` | string | `"end"` | FAB position |
 *
 * @param component The [UIComponent] whose `type` is `"scaffold"`.
 *
 * @see RenderTopAppBar
 * @see RenderBottomAppBar
 * @see RenderFloatingActionButton
 */@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderScaffold(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
        ?: MaterialTheme.colorScheme.background
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
        ?: MaterialTheme.colorScheme.onBackground
    val contentWindowInsets = props["contentWindowInsets"]?.jsonObject
        ?.let { parseWindowInsets(it) } ?: ScaffoldDefaults.contentWindowInsets

    val topBarContent = props["topBar"]?.jsonArray
    val bottomBarContent = props["bottomBar"]?.jsonArray
    val snackbarHostContent = props["snackbarHost"]?.jsonArray
    val fabContent = props["floatingActionButton"]?.jsonArray
    val fabPosition = parseFabPosition(
        props["floatingActionButtonPosition"]?.jsonPrimitive?.contentOrNull
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            topBarContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
        },
        bottomBar = {
            bottomBarContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
        },
        snackbarHost = {
            if (snackbarHostContent != null) {
                snackbarHostContent.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            } else {
                SnackbarHost(hostState = remember { SnackbarHostState() })
            }
        },
        floatingActionButton = {
            fabContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
        },
        floatingActionButtonPosition = fabPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            component.children?.forEach { child -> RenderComponent(child) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  TopAppBar  (small | centerAligned | medium | large)
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 top app bar from a [UIComponent] node.
 *
 * The `type` prop selects the variant:
 * - `"small"` (default) — `TopAppBar`
 * - `"centerAligned"` — `CenterAlignedTopAppBar`
 * - `"medium"` — `MediumTopAppBar`
 * - `"large"` — `LargeTopAppBar`
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `type` | string | `"small"` | Bar variant |
 * | `title` | array | – | Title slot components |
 * | `navigationIcon` | array | – | Leading icon slot |
 * | `actions` | array | – | Trailing action icons |
 * | `colors` | object | defaults | Custom bar colours |
 * | `windowInsets` | object | defaults | Window insets override |
 * | `scrollBehavior` | object | – | Scroll-behaviour configuration |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"topappbar"`.
 *
 * @see RenderScaffold
 * @see RenderAppBarAction
 */@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderTopAppBar(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val type = props["type"]?.jsonPrimitive?.contentOrNull ?: "small"
    val colors = props["colors"]?.jsonObject?.let { parseTopAppBarColors(it) }
        ?: TopAppBarDefaults.topAppBarColors()
    val windowInsets = props["windowInsets"]?.jsonObject?.let { parseWindowInsets(it) }
        ?: TopAppBarDefaults.windowInsets
    val scrollBehavior = props["scrollBehavior"]?.jsonObject
        ?.let { parseTopAppBarScrollBehavior(it) }

    val title: @Composable () -> Unit = {
        props["title"]?.jsonArray?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
    }
    val navIcon: @Composable () -> Unit = {
        props["navigationIcon"]?.jsonArray?.forEach {
            RenderComponent(Json.decodeFromJsonElement(it))
        }
    }
    val actions: @Composable RowScope.() -> Unit = {
        props["actions"]?.jsonArray?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
    }

    when (type) {
        "centerAligned" -> CenterAlignedTopAppBar(
            title = title, modifier = modifier, navigationIcon = navIcon,
            actions = actions, windowInsets = windowInsets,
            colors = colors, scrollBehavior = scrollBehavior
        )
        "medium" -> MediumTopAppBar(
            title = title, modifier = modifier, navigationIcon = navIcon,
            actions = actions, windowInsets = windowInsets,
            colors = colors, scrollBehavior = scrollBehavior
        )
        "large" -> LargeTopAppBar(
            title = title, modifier = modifier, navigationIcon = navIcon,
            actions = actions, windowInsets = windowInsets,
            colors = colors, scrollBehavior = scrollBehavior
        )
        else -> TopAppBar(
            title = title, modifier = modifier, navigationIcon = navIcon,
            actions = actions, windowInsets = windowInsets,
            colors = colors, scrollBehavior = scrollBehavior
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  BottomAppBar
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 `BottomAppBar` from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `containerColor` | string | defaults | Bar background colour |
 * | `contentColor` | string | unspecified | Default icon/text colour |
 * | `tonalElevation` | int | defaults | Elevation in dp |
 * | `contentPadding` | object/int | defaults | Inner padding |
 * | `windowInsets` | object | defaults | Window insets override |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * Children are rendered inside the bar’s content slot.
 *
 * @param component The [UIComponent] whose `type` is `"bottomappbar"`.
 *
 * @see RenderScaffold
 */@Composable
internal fun RenderBottomAppBar(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
        ?: BottomAppBarDefaults.containerColor
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
        ?: Color.Unspecified
    val tonalElevation = props["tonalElevation"]?.jsonPrimitive?.intOrNull?.dp
        ?: BottomAppBarDefaults.ContainerElevation
    val contentPadding = props["contentPadding"]?.let { parsePadding(it) }
        ?: BottomAppBarDefaults.ContentPadding
    val windowInsets = props["windowInsets"]?.jsonObject?.let { parseWindowInsets(it) }
        ?: BottomAppBarDefaults.windowInsets

    BottomAppBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        contentPadding = contentPadding,
        windowInsets = windowInsets
    ) {
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  NavigationBar  (contains NavigationBarItem children)
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 `NavigationBar` from a [UIComponent] node.
 *
 * Children whose type is `"NavigationBarItem"` are rendered via
 * [RenderNavigationBarItem] inside the required `RowScope`; all other
 * children fall back to [RenderComponent].
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `containerColor` | string | defaults | Bar background colour |
 * | `contentColor` | string | unspecified | Default content colour |
 * | `tonalElevation` | int | defaults | Elevation in dp |
 * | `windowInsets` | object | defaults | Window insets override |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"navigationbar"`.
 *
 * @see RenderNavigationBarItem
 * @see RenderScaffold
 */@Composable
internal fun RenderNavigationBar(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
        ?: NavigationBarDefaults.containerColor
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
        ?: Color.Unspecified
    val tonalElevation = props["tonalElevation"]?.jsonPrimitive?.intOrNull?.dp
        ?: NavigationBarDefaults.Elevation
    val windowInsets = props["windowInsets"]?.jsonObject?.let { parseWindowInsets(it) }
        ?: NavigationBarDefaults.windowInsets

    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets
    ) {
        // Children MUST be NavigationBarItem — rendered inside RowScope
        component.children?.forEach { child ->
            if (child.type.equals("NavigationBarItem", ignoreCase = true)) {
                RenderNavigationBarItem(child)
            } else {
                RenderComponent(child)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  NavigationBarItem  (must be called inside RowScope)
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a single `NavigationBarItem` inside a `RowScope`.
 *
 * Must be invoked from within a [NavigationBar] (i.e. [RenderNavigationBar]).
 * Switches between `icon` and `selectedIcon` content slots based on [selected].
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `selected` | boolean | `false` | Whether the item is active |
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `icon` | array | – | Unselected icon slot |
 * | `selectedIcon` | array | – | Selected icon slot |
 * | `label` | array | – | Label slot |
 * | `enabled` | boolean | `true` | Whether taps are accepted |
 * | `alwaysShowLabel` | boolean | `true` | Show label even when unselected |
 * | `colors` | object | defaults | Custom item colours |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"navigationbaritem"`.
 *
 * @see RenderNavigationBar
 */@Composable
internal fun RowScope.RenderNavigationBarItem(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val selected = props["selected"]?.jsonPrimitive?.booleanOrNull ?: false
    val context = LocalContext.current
    val navController = LocalKetoyNavController.current
    val onClickAction = OnClickResolver.resolve(props["onClick"], context, navController)
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val alwaysShowLabel = props["alwaysShowLabel"]?.jsonPrimitive?.booleanOrNull ?: true

    val iconContent = props["icon"]?.jsonArray
    val selectedIconContent = props["selectedIcon"]?.jsonArray
    val labelContent = props["label"]?.jsonArray

    val colors = props["colors"]?.jsonObject?.let { parseNavigationBarItemColors(it) }
        ?: NavigationBarItemDefaults.colors()

    NavigationBarItem(
        selected = selected,
        onClick = { onClickAction?.invoke() },
        icon = {
            if (selected && selectedIconContent != null) {
                selectedIconContent.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            } else {
                iconContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            }
        },
        modifier = modifier,
        enabled = enabled,
        label = labelContent?.let { arr ->
            { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
        },
        alwaysShowLabel = alwaysShowLabel,
        colors = colors
    )
}

// ═══════════════════════════════════════════════════════════════════
//  NavigationDrawerItem
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 `NavigationDrawerItem` from a [UIComponent] node.
 *
 * Typically used inside a `ModalNavigationDrawer` or `PermanentNavigationDrawer`.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `selected` | boolean | `false` | Whether the item is active |
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `icon` | array | – | Leading icon slot |
 * | `label` | array | – | Label slot |
 * | `badge` | array | – | Trailing badge slot |
 * | `colors` | object | defaults | Custom item colours |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"navigationdraweritem"`.
 */@Composable
internal fun RenderNavigationDrawerItem(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val selected = props["selected"]?.jsonPrimitive?.booleanOrNull ?: false
    val context = LocalContext.current
    val navController = LocalKetoyNavController.current
    val onClickAction = OnClickResolver.resolve(props["onClick"], context, navController)
    val modifier = parseModifier(props)
    val colors = props["colors"]?.jsonObject?.let { parseNavigationDrawerItemColors(it) }
        ?: NavigationDrawerItemDefaults.colors()

    val iconContent = props["icon"]?.jsonArray
    val labelContent = props["label"]?.jsonArray
    val badgeContent = props["badge"]?.jsonArray

    NavigationDrawerItem(
        label = { labelContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } },
        selected = selected,
        onClick = { onClickAction?.invoke() },
        modifier = modifier,
        icon = iconContent?.let { arr ->
            { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
        },
        badge = badgeContent?.let { arr ->
            { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
        },
        colors = colors
    )
}

// ═══════════════════════════════════════════════════════════════════
//  CustomNavigationItem  (fallback custom button-based nav item)
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a custom button-based navigation item as a fallback when the
 * standard Material `NavigationBarItem` is not flexible enough.
 *
 * Uses a `Button` with a `Column` of icon + label to emulate a nav-item.
 * Supports distinct colours for selected vs. unselected states.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `selected` | boolean | `false` | Active state |
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `icon` | array | – | Unselected icon slot |
 * | `selectedIcon` | array | – | Selected icon slot |
 * | `label` | array | – | Label slot |
 * | `enabled` | boolean | `true` | Whether taps are accepted |
 * | `alwaysShowLabel` | boolean | `true` | Show label when unselected |
 * | `containerColor` | string | `surface` | Normal background |
 * | `selectedContainerColor` | string | `primaryContainer` | Active background |
 * | `contentColor` | string | `onSurface` | Normal icon/text colour |
 * | `selectedContentColor` | string | `onPrimaryContainer` | Active colour |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"customnavigationitem"`.
 *
 * @see RenderNavigationBarItem
 */@Composable
internal fun RenderCustomNavigationItem(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val selected = props["selected"]?.jsonPrimitive?.booleanOrNull ?: false
    val context = LocalContext.current
    val navController = LocalKetoyNavController.current
    val onClickAction = OnClickResolver.resolve(props["onClick"], context, navController)
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val alwaysShowLabel = props["alwaysShowLabel"]?.jsonPrimitive?.booleanOrNull ?: true

    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
    val selectedContainerColor = resolveKetoyColorOrNull(props["selectedContainerColor"]?.jsonPrimitive?.contentOrNull)
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
    val selectedContentColor = resolveKetoyColorOrNull(props["selectedContentColor"]?.jsonPrimitive?.contentOrNull)

    val iconContent = props["icon"]?.jsonArray
    val selectedIconContent = props["selectedIcon"]?.jsonArray
    val labelContent = props["label"]?.jsonArray

    Button(
        onClick = { onClickAction?.invoke() },
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                selectedContainerColor ?: containerColor ?: MaterialTheme.colorScheme.primaryContainer
            } else {
                containerColor ?: MaterialTheme.colorScheme.surface
            },
            contentColor = if (selected) {
                selectedContentColor ?: contentColor ?: MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                contentColor ?: MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (selected && selectedIconContent != null) {
                selectedIconContent.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            } else {
                iconContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            }
            if ((alwaysShowLabel || selected) && labelContent != null) {
                labelContent.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  FloatingActionButton  (regular | small | large | extended)
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 Floating Action Button from a [UIComponent] node.
 *
 * The `type` prop selects the FAB variant:
 * - `"regular"` (default) — `FloatingActionButton`
 * - `"small"` — `SmallFloatingActionButton`
 * - `"large"` — `LargeFloatingActionButton`
 * - `"extended"` — `ExtendedFloatingActionButton` (splits children into
 *   text and icon slots automatically)
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `type` | string | `"regular"` | FAB variant |
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `shape` | string | defaults | Corner shape descriptor |
 * | `containerColor` | string | defaults | Background colour |
 * | `contentColor` | string | unspecified | Icon/text colour |
 * | `elevation` | object | defaults | Elevation configuration |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"floatingactionbutton"`.
 *
 * @see RenderScaffold
 */@Composable
internal fun RenderFloatingActionButton(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val context = LocalContext.current
    val navController = LocalKetoyNavController.current
    val onClickAction = OnClickResolver.resolve(props["onClick"], context, navController)
    val modifier = parseModifier(props)
    val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) }
        ?: FloatingActionButtonDefaults.shape
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
        ?: FloatingActionButtonDefaults.containerColor
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
        ?: Color.Unspecified
    val elevation = props["elevation"]?.jsonObject?.let { parseFabElevation(it) }
        ?: FloatingActionButtonDefaults.elevation()
    val type = props["type"]?.jsonPrimitive?.contentOrNull ?: "regular"

    val content: @Composable () -> Unit = {
        component.children?.forEach { child -> RenderComponent(child) }
    }

    when (type) {
        "small" -> SmallFloatingActionButton(
            onClick = { onClickAction?.invoke() }, modifier = modifier,
            shape = shape, containerColor = containerColor,
            contentColor = contentColor, elevation = elevation,
            content = content
        )
        "large" -> LargeFloatingActionButton(
            onClick = { onClickAction?.invoke() }, modifier = modifier,
            shape = shape, containerColor = containerColor,
            contentColor = contentColor, elevation = elevation,
            content = content
        )
        "extended" -> ExtendedFloatingActionButton(
            onClick = { onClickAction?.invoke() }, modifier = modifier,
            shape = shape, containerColor = containerColor,
            contentColor = contentColor, elevation = elevation,
            text = {
                component.children?.filter { it.type.equals("Text", ignoreCase = true) }
                    ?.forEach { RenderComponent(it) }
            },
            icon = {
                component.children?.filter { !it.type.equals("Text", ignoreCase = true) }
                    ?.forEach { RenderComponent(it) }
            }
        )
        else -> FloatingActionButton(
            onClick = { onClickAction?.invoke() }, modifier = modifier,
            shape = shape, containerColor = containerColor,
            contentColor = contentColor, elevation = elevation,
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Snackbar
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 `Snackbar` from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `message` | string | `""` | Text shown inside the snackbar |
 * | `actionOnNewLine` | boolean | `false` | Place the action on a new line |
 * | `shape` | string | defaults | Corner shape descriptor |
 * | `containerColor` | string | defaults | Background colour |
 * | `contentColor` | string | defaults | Text colour |
 * | `actionContentColor` | string | defaults | Action button colour |
 * | `dismissActionContentColor` | string | defaults | Dismiss icon colour |
 * | `action` | array | – | Action-button slot |
 * | `dismissAction` | array | – | Dismiss-button slot |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"snackbar"`.
 *
 * @see RenderSnackBarHost
 */@Composable
internal fun RenderSnackBar(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val actionOnNewLine = props["actionOnNewLine"]?.jsonPrimitive?.booleanOrNull ?: false
    val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) }
        ?: SnackbarDefaults.shape
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
        ?: SnackbarDefaults.color
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
        ?: SnackbarDefaults.contentColor
    val actionContentColor = resolveKetoyColorOrNull(props["actionContentColor"]?.jsonPrimitive?.contentOrNull)
        ?: SnackbarDefaults.actionContentColor
    val dismissActionContentColor = resolveKetoyColorOrNull(props["dismissActionContentColor"]?.jsonPrimitive?.contentOrNull)
        ?: SnackbarDefaults.dismissActionContentColor
    val message = props["message"]?.jsonPrimitive?.contentOrNull ?: ""

    val actionContent = props["action"]?.jsonArray
    val dismissActionContent = props["dismissAction"]?.jsonArray

    Snackbar(
        modifier = modifier,
        action = actionContent?.let { arr ->
            { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
        },
        dismissAction = dismissActionContent?.let { arr ->
            { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
        },
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor
    ) {
        if (message.isNotEmpty()) Text(text = message)
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  SnackbarHost
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a `SnackbarHost` from a [UIComponent] node.
 *
 * If a `snackbar` array is provided in props, those components are used as
 * the host’s snackbar content; otherwise a default empty host is created.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `snackbar` | array | – | Custom snackbar content |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"snackbarhost"`.
 *
 * @see RenderSnackBar
 */@Composable
internal fun RenderSnackBarHost(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val snackbarContent = props["snackbar"]?.jsonArray
    val hostState = remember { SnackbarHostState() }

    if (snackbarContent != null) {
        SnackbarHost(hostState = hostState, modifier = modifier) {
            snackbarContent.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
        }
    } else {
        SnackbarHost(hostState = hostState, modifier = modifier)
    }
}

// ═══════════════════════════════════════════════════════════════════
//  AppBarAction (IconButton wrapper for TopAppBar actions)
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders an `IconButton` intended for use inside a [TopAppBar] actions slot.
 *
 * Children are placed inside the button's content area (typically a single
 * `Icon` widget).
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `enabled` | boolean | `true` | Whether taps are accepted |
 * | `colors` | object | defaults | Icon-button colour overrides |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"appbaraction"`.
 *
 * @see RenderTopAppBar
 */@Composable
internal fun RenderAppBarAction(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val colors = props["colors"]?.jsonObject?.let { parseIconButtonColors(it) }
        ?: IconButtonDefaults.iconButtonColors()
    val context = LocalContext.current
    val navController = LocalKetoyNavController.current
    val resolvedClick = OnClickResolver.resolve(props["onClick"], context, navController)

    IconButton(
        onClick = { resolvedClick?.invoke() },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  NavigationRail
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 `NavigationRail` from a [UIComponent] node.
 *
 * The rail is typically used on larger screens (tablets, desktops) as a
 * side-navigation alternative to `NavigationBar`.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `containerColor` | string | defaults | Rail background colour |
 * | `contentColor` | string | unspecified | Default content colour |
 * | `header` | array | – | Header slot (e.g. FAB or logo) |
 * | `windowInsets` | object | defaults | Window insets override |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * Children are rendered as rail items.
 *
 * @param component The [UIComponent] whose `type` is `"navigationrail"`.
 *
 * @see RenderNavigationRailItem
 */@Composable
internal fun RenderNavigationRail(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
        ?: NavigationRailDefaults.ContainerColor
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
        ?: Color.Unspecified
    val headerContent = props["header"]?.jsonArray
    val windowInsets = props["windowInsets"]?.jsonObject?.let { parseWindowInsets(it) }
        ?: NavigationRailDefaults.windowInsets

    NavigationRail(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        header = headerContent?.let { arr ->
            { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
        },
        windowInsets = windowInsets
    ) {
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  NavigationRailItem
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a single `NavigationRailItem` from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `selected` | boolean | `false` | Whether the item is active |
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `icon` | array | – | Unselected icon slot |
 * | `selectedIcon` | array | – | Selected icon slot |
 * | `label` | array | – | Label slot |
 * | `enabled` | boolean | `true` | Whether taps are accepted |
 * | `alwaysShowLabel` | boolean | `true` | Show label when unselected |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"navigationrailitem"`.
 *
 * @see RenderNavigationRail
 */@Composable
internal fun RenderNavigationRailItem(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val selected = props["selected"]?.jsonPrimitive?.booleanOrNull ?: false
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val alwaysShowLabel = props["alwaysShowLabel"]?.jsonPrimitive?.booleanOrNull ?: true

    val iconContent = props["icon"]?.jsonArray
    val selectedIconContent = props["selectedIcon"]?.jsonArray
    val labelContent = props["label"]?.jsonArray
    val context = LocalContext.current
    val navController = LocalKetoyNavController.current
    val resolvedClick = OnClickResolver.resolve(props["onClick"], context, navController)

    NavigationRailItem(
        selected = selected,
        onClick = { resolvedClick?.invoke() },
        icon = {
            if (selected && selectedIconContent != null) {
                selectedIconContent.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            } else {
                iconContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            }
        },
        modifier = modifier,
        enabled = enabled,
        label = labelContent?.let { arr ->
            { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
        },
        alwaysShowLabel = alwaysShowLabel
    )
}

// ═══════════════════════════════════════════════════════════════════
//  ModalBottomSheet
// ═══════════════════════════════════════════════════════════════════
/**
 * Renders a Material 3 `ModalBottomSheet` from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `shape` | string | expanded shape | Corner shape descriptor |
 * | `containerColor` | string | defaults | Sheet background colour |
 * | `contentColor` | string | unspecified | Default content colour |
 * | `tonalElevation` | int | defaults | Elevation in dp |
 * | `scrimColor` | string | defaults | Overlay scrim colour |
 * | `dragHandle` | array | default drag handle | Custom drag-handle slot |
 * | `onDismissRequest` | string/object | – | Dismiss action via [OnClickResolver] |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * Children are rendered inside the sheet body.
 *
 * @param component The [UIComponent] whose `type` is `"modalbottomsheet"`.
 */@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderModalBottomSheet(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) }
        ?: BottomSheetDefaults.ExpandedShape
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
        ?: BottomSheetDefaults.ContainerColor
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
        ?: Color.Unspecified
    val tonalElevation = props["tonalElevation"]?.jsonPrimitive?.intOrNull?.dp
        ?: BottomSheetDefaults.Elevation
    val scrimColor = resolveKetoyColorOrNull(props["scrimColor"]?.jsonPrimitive?.contentOrNull)
        ?: BottomSheetDefaults.ScrimColor
    val dragHandleContent = props["dragHandle"]?.jsonArray
    val context = LocalContext.current
    val navController = LocalKetoyNavController.current
    val resolvedDismiss = OnClickResolver.resolve(props["onDismissRequest"], context, navController)

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { resolvedDismiss?.invoke() },
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandleContent?.let { arr ->
            { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
        } ?: { BottomSheetDefaults.DragHandle() }
    ) {
        component.children?.forEach { child -> RenderComponent(child) }
    }
}
