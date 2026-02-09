package com.developerstring.ketoy.renderer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.parser.*
import kotlinx.serialization.json.*

// ─── Scaffold ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderScaffold(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        ?: MaterialTheme.colorScheme.background
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        ?: MaterialTheme.colorScheme.onBackground
    val contentWindowInsets = props["contentWindowInsets"]?.jsonObject?.let { parseWindowInsets(it) }
        ?: ScaffoldDefaults.contentWindowInsets

    val topBarContent = props["topBar"]?.jsonArray
    val bottomBarContent = props["bottomBar"]?.jsonArray
    val snackbarHostContent = props["snackbarHost"]?.jsonArray
    val fabContent = props["floatingActionButton"]?.jsonArray
    val fabPosition = parseFabPosition(props["floatingActionButtonPosition"]?.jsonPrimitive?.content)

    Scaffold(
        modifier = modifier,
        topBar = { topBarContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } },
        bottomBar = { bottomBarContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } },
        snackbarHost = {
            if (snackbarHostContent != null) {
                snackbarHostContent.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            } else {
                SnackbarHost(hostState = remember { SnackbarHostState() })
            }
        },
        floatingActionButton = { fabContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } },
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

// ─── TopAppBar ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderTopAppBar(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val colors = props["colors"]?.jsonObject?.let { parseTopAppBarColors(it) }
        ?: TopAppBarDefaults.topAppBarColors()
    val windowInsets = props["windowInsets"]?.jsonObject?.let { parseWindowInsets(it) }
        ?: TopAppBarDefaults.windowInsets
    val scrollBehavior = props["scrollBehavior"]?.jsonObject?.let { parseTopAppBarScrollBehavior(it) }
    val type = props["type"]?.jsonPrimitive?.content ?: "small"

    val titleContent = props["title"]?.jsonArray
    val navIconContent = props["navigationIcon"]?.jsonArray
    val actionsContent = props["actions"]?.jsonArray

    val title: @Composable () -> Unit = { titleContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
    val navIcon: @Composable () -> Unit = { navIconContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }
    val actions: @Composable RowScope.() -> Unit = { actionsContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } }

    when (type) {
        "small" -> TopAppBar(title = title, modifier = modifier, navigationIcon = navIcon, actions = actions, windowInsets = windowInsets, colors = colors, scrollBehavior = scrollBehavior)
        "centerAligned" -> CenterAlignedTopAppBar(title = title, modifier = modifier, navigationIcon = navIcon, actions = actions, windowInsets = windowInsets, colors = colors, scrollBehavior = scrollBehavior)
        "medium" -> MediumTopAppBar(title = title, modifier = modifier, navigationIcon = navIcon, actions = actions, windowInsets = windowInsets, colors = colors, scrollBehavior = scrollBehavior)
        "large" -> LargeTopAppBar(title = title, modifier = modifier, navigationIcon = navIcon, actions = actions, windowInsets = windowInsets, colors = colors, scrollBehavior = scrollBehavior)
    }
}

// ─── BottomAppBar ─────────────────────────────────────────────────

@Composable
internal fun RenderBottomAppBar(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        ?: BottomAppBarDefaults.containerColor
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
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

// ─── NavigationBar ────────────────────────────────────────────────

@Composable
internal fun RenderNavigationBar(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        ?: NavigationBarDefaults.containerColor
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
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
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ─── NavigationDrawerItem ─────────────────────────────────────────

@Composable
internal fun RenderNavigationDrawerItem(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val selected = props["selected"]?.jsonPrimitive?.booleanOrNull ?: false
    val onClick = props["onClick"]?.jsonPrimitive?.contentOrNull
    val modifier = parseModifier(props)
    val colors = props["colors"]?.jsonObject?.let { parseNavigationDrawerItemColors(it) }
        ?: NavigationDrawerItemDefaults.colors()

    val iconContent = props["icon"]?.jsonArray
    val labelContent = props["label"]?.jsonArray
    val badgeContent = props["badge"]?.jsonArray

    NavigationDrawerItem(
        label = { labelContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } },
        selected = selected,
        onClick = { onClick?.let { ActionRegistry.get(it)?.invoke() } },
        modifier = modifier,
        icon = iconContent?.let { arr -> { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } } },
        badge = badgeContent?.let { arr -> { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } } },
        colors = colors
    )
}

// ─── CustomNavigationItem ─────────────────────────────────────────

@Composable
internal fun RenderCustomNavigationItem(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val selected = props["selected"]?.jsonPrimitive?.booleanOrNull ?: false
    val onClick = props["onClick"]?.jsonPrimitive?.contentOrNull
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val alwaysShowLabel = props["alwaysShowLabel"]?.jsonPrimitive?.booleanOrNull ?: true

    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val selectedContainerColor = props["selectedContainerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val selectedContentColor = props["selectedContentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }

    val iconContent = props["icon"]?.jsonArray
    val selectedIconContent = props["selectedIcon"]?.jsonArray
    val labelContent = props["label"]?.jsonArray

    Button(
        onClick = { onClick?.let { ActionRegistry.get(it)?.invoke() } },
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) (selectedContainerColor ?: containerColor ?: MaterialTheme.colorScheme.primaryContainer)
                else (containerColor ?: MaterialTheme.colorScheme.surface),
            contentColor = if (selected) (selectedContentColor ?: contentColor ?: MaterialTheme.colorScheme.onPrimaryContainer)
                else (contentColor ?: MaterialTheme.colorScheme.onSurface)
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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

// ─── FloatingActionButton ─────────────────────────────────────────

@Composable
internal fun RenderFloatingActionButton(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val onClick = props["onClick"]?.jsonPrimitive?.contentOrNull
    val modifier = parseModifier(props)
    val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) }
        ?: FloatingActionButtonDefaults.shape
    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        ?: FloatingActionButtonDefaults.containerColor
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        ?: Color.Unspecified
    val elevation = props["elevation"]?.jsonObject?.let { parseFabElevation(it) }
        ?: FloatingActionButtonDefaults.elevation()
    val type = props["type"]?.jsonPrimitive?.content ?: "regular"

    val onClickAction: () -> Unit = { onClick?.let { ActionRegistry.get(it)?.invoke() } }
    val content: @Composable () -> Unit = { component.children?.forEach { child -> RenderComponent(child) } }

    when (type) {
        "small" -> SmallFloatingActionButton(onClick = onClickAction, modifier = modifier, shape = shape, containerColor = containerColor, contentColor = contentColor, elevation = elevation, content = content)
        "large" -> LargeFloatingActionButton(onClick = onClickAction, modifier = modifier, shape = shape, containerColor = containerColor, contentColor = contentColor, elevation = elevation, content = content)
        "extended" -> ExtendedFloatingActionButton(
            onClick = onClickAction, modifier = modifier, shape = shape,
            containerColor = containerColor, contentColor = contentColor, elevation = elevation,
            text = { component.children?.filter { it.type.lowercase() == "text" }?.forEach { RenderComponent(it) } },
            icon = { component.children?.filter { it.type.lowercase() != "text" }?.forEach { RenderComponent(it) } }
        )
        else -> FloatingActionButton(onClick = onClickAction, modifier = modifier, shape = shape, containerColor = containerColor, contentColor = contentColor, elevation = elevation, content = content)
    }
}

// ─── SnackBar ─────────────────────────────────────────────────────

@Composable
internal fun RenderSnackBar(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val actionOnNewLine = props["actionOnNewLine"]?.jsonPrimitive?.booleanOrNull ?: false
    val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) } ?: SnackbarDefaults.shape
    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) } ?: SnackbarDefaults.color
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) } ?: SnackbarDefaults.contentColor
    val actionContentColor = props["actionContentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) } ?: SnackbarDefaults.actionContentColor
    val dismissActionContentColor = props["dismissActionContentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) } ?: SnackbarDefaults.dismissActionContentColor
    val message = props["message"]?.jsonPrimitive?.contentOrNull ?: ""

    val actionContent = props["action"]?.jsonArray
    val dismissActionContent = props["dismissAction"]?.jsonArray

    Snackbar(
        modifier = modifier,
        action = actionContent?.let { arr -> { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } } },
        dismissAction = dismissActionContent?.let { arr -> { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } } },
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

// ─── SnackBarHost ─────────────────────────────────────────────────

@Composable
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

// ─── AppBarAction ─────────────────────────────────────────────────

@Composable
internal fun RenderAppBarAction(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val onClick = props["onClick"]?.jsonPrimitive?.contentOrNull
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val colors = props["colors"]?.jsonObject?.let { parseIconButtonColors(it) }
        ?: IconButtonDefaults.iconButtonColors()

    IconButton(
        onClick = { onClick?.let { ActionRegistry.get(it)?.invoke() } },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ─── NavigationRail ───────────────────────────────────────────────

@Composable
internal fun RenderNavigationRail(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        ?: NavigationRailDefaults.ContainerColor
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        ?: Color.Unspecified
    val headerContent = props["header"]?.jsonArray
    val windowInsets = props["windowInsets"]?.jsonObject?.let { parseWindowInsets(it) }
        ?: NavigationRailDefaults.windowInsets

    NavigationRail(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        header = headerContent?.let { arr -> { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } } },
        windowInsets = windowInsets
    ) {
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ─── NavigationRailItem ───────────────────────────────────────────

@Composable
internal fun RenderNavigationRailItem(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val selected = props["selected"]?.jsonPrimitive?.booleanOrNull ?: false
    val onClick = props["onClick"]?.jsonPrimitive?.contentOrNull
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val alwaysShowLabel = props["alwaysShowLabel"]?.jsonPrimitive?.booleanOrNull ?: true

    val iconContent = props["icon"]?.jsonArray
    val selectedIconContent = props["selectedIcon"]?.jsonArray
    val labelContent = props["label"]?.jsonArray

    NavigationRailItem(
        selected = selected,
        onClick = { onClick?.let { ActionRegistry.get(it)?.invoke() } },
        icon = {
            if (selected && selectedIconContent != null) {
                selectedIconContent.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            } else {
                iconContent?.forEach { RenderComponent(Json.decodeFromJsonElement(it)) }
            }
        },
        modifier = modifier,
        enabled = enabled,
        label = labelContent?.let { arr -> { arr.forEach { RenderComponent(Json.decodeFromJsonElement(it)) } } },
        alwaysShowLabel = alwaysShowLabel
    )
}
