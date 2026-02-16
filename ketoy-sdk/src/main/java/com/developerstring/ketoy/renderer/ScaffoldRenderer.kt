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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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

@Composable
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

@Composable
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

@Composable
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

@Composable
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

@Composable
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

@Composable
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

@Composable
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

// ═══════════════════════════════════════════════════════════════════
//  AppBarAction (IconButton wrapper for TopAppBar actions)
// ═══════════════════════════════════════════════════════════════════

@Composable
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

@Composable
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

@Composable
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

@OptIn(ExperimentalMaterial3Api::class)
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
