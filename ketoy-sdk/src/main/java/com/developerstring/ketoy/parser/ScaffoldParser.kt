/**
 * Scaffold-component parser for the Ketoy SDUI engine.
 *
 * Contains composable and non-composable helpers that convert JSON objects
 * into the Compose Material 3 types required by `Scaffold`, `TopAppBar`,
 * `NavigationDrawer`, `NavigationBar`, and `FloatingActionButton` components.
 *
 * @see resolveKetoyColor
 * @see parseShape
 */
package com.developerstring.ketoy.parser

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.mandatorySystemGestures
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.waterfall
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*

// ─── Window Insets ────────────────────────────────────────────────

/**
 * Parses a window-insets JSON object into a Compose [WindowInsets] instance.
 *
 * When `type` is specified, the corresponding system-defined insets are
 * returned (e.g. `"statusBars"`, `"navigationBars"`, `"ime"`).
 * Otherwise, explicit `left`, `top`, `right`, `bottom` dp values are used.
 *
 * @param windowInsetsObject the JSON descriptor.
 * @return the resolved [WindowInsets].
 */
@Composable
fun parseWindowInsets(windowInsetsObject: JsonObject): WindowInsets {
    val type = windowInsetsObject["type"]?.jsonPrimitive?.content
    val left = windowInsetsObject["left"]?.jsonPrimitive?.intOrNull ?: 0
    val top = windowInsetsObject["top"]?.jsonPrimitive?.intOrNull ?: 0
    val right = windowInsetsObject["right"]?.jsonPrimitive?.intOrNull ?: 0
    val bottom = windowInsetsObject["bottom"]?.jsonPrimitive?.intOrNull ?: 0

    return when (type) {
        "statusBars" -> WindowInsets.statusBars
        "navigationBars" -> WindowInsets.navigationBars
        "systemBars" -> WindowInsets.systemBars
        "ime" -> WindowInsets.ime
        "captionBar" -> WindowInsets.captionBar
        "displayCutout" -> WindowInsets.displayCutout
        "mandatorySystemGestures" -> WindowInsets.mandatorySystemGestures
        "systemGestures" -> WindowInsets.systemGestures
        "tappableElement" -> WindowInsets.tappableElement
        "waterfall" -> WindowInsets.waterfall
        else -> WindowInsets(left, top, right, bottom)
    }
}

// ─── TopAppBar Colours ────────────────────────────────────────────

/**
 * Parses a JSON object into [TopAppBarColors] for Material 3 top app bars.
 *
 * Recognised keys: `containerColor`, `scrolledContainerColor`,
 * `navigationIconContentColor`, `titleContentColor`, `actionIconContentColor`.
 * All values are resolved via [resolveKetoyColor].
 *
 * @param colorsObject the JSON descriptor.
 * @return the configured [TopAppBarColors].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun parseTopAppBarColors(colorsObject: JsonObject): TopAppBarColors {
    return TopAppBarDefaults.topAppBarColors(
        containerColor = resolveKetoyColor(colorsObject["containerColor"]?.jsonPrimitive?.content),
        scrolledContainerColor = resolveKetoyColor(colorsObject["scrolledContainerColor"]?.jsonPrimitive?.content),
        navigationIconContentColor = resolveKetoyColor(colorsObject["navigationIconContentColor"]?.jsonPrimitive?.content),
        titleContentColor = resolveKetoyColor(colorsObject["titleContentColor"]?.jsonPrimitive?.content),
        actionIconContentColor = resolveKetoyColor(colorsObject["actionIconContentColor"]?.jsonPrimitive?.content)
    )
}

// ─── TopAppBar Scroll Behaviour ───────────────────────────────────

/**
 * Parses a JSON object into a [TopAppBarScrollBehavior].
 *
 * Recognised `type` values: `"pinnedScroll"`, `"enterAlwaysScroll"`,
 * `"exitUntilCollapsedScroll"`.
 *
 * @param scrollBehaviorObject the JSON descriptor.
 * @return the matching [TopAppBarScrollBehavior], or `null` for unknown types.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun parseTopAppBarScrollBehavior(scrollBehaviorObject: JsonObject): TopAppBarScrollBehavior? {
    return when (scrollBehaviorObject["type"]?.jsonPrimitive?.content) {
        "pinnedScroll" -> TopAppBarDefaults.pinnedScrollBehavior()
        "enterAlwaysScroll" -> TopAppBarDefaults.enterAlwaysScrollBehavior()
        "exitUntilCollapsedScroll" -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        else -> null
    }
}

// ─── NavigationDrawerItem Colours ─────────────────────────────────

/**
 * Parses a JSON object into [NavigationDrawerItemColors].
 *
 * Recognised keys: `selectedContainerColor`, `unselectedContainerColor`,
 * `selectedIconColor`, `unselectedIconColor`, `selectedTextColor`,
 * `unselectedTextColor`, `selectedBadgeColor`, `unselectedBadgeColor`.
 *
 * @param colorsObject the JSON descriptor.
 * @return the configured [NavigationDrawerItemColors].
 */
@Composable
fun parseNavigationDrawerItemColors(colorsObject: JsonObject): NavigationDrawerItemColors {
    return NavigationDrawerItemDefaults.colors(
        selectedContainerColor = resolveKetoyColor(colorsObject["selectedContainerColor"]?.jsonPrimitive?.content),
        unselectedContainerColor = resolveKetoyColor(colorsObject["unselectedContainerColor"]?.jsonPrimitive?.content),
        selectedIconColor = resolveKetoyColor(colorsObject["selectedIconColor"]?.jsonPrimitive?.content),
        unselectedIconColor = resolveKetoyColor(colorsObject["unselectedIconColor"]?.jsonPrimitive?.content),
        selectedTextColor = resolveKetoyColor(colorsObject["selectedTextColor"]?.jsonPrimitive?.content),
        unselectedTextColor = resolveKetoyColor(colorsObject["unselectedTextColor"]?.jsonPrimitive?.content),
        selectedBadgeColor = resolveKetoyColor(colorsObject["selectedBadgeColor"]?.jsonPrimitive?.content),
        unselectedBadgeColor = resolveKetoyColor(colorsObject["unselectedBadgeColor"]?.jsonPrimitive?.content)
    )
}

// ─── IconButton Colours ───────────────────────────────────────────

/**
 * Parses a JSON object into Material 3 [IconButtonColors].
 *
 * Recognised keys: `containerColor`, `contentColor`,
 * `disabledContainerColor`, `disabledContentColor`.
 *
 * @param colorsObject the JSON descriptor.
 * @return the configured [IconButtonColors].
 */
@Composable
fun parseIconButtonColors(colorsObject: JsonObject): IconButtonColors {
    return IconButtonDefaults.iconButtonColors(
        containerColor = resolveKetoyColor(colorsObject["containerColor"]?.jsonPrimitive?.content),
        contentColor = resolveKetoyColor(colorsObject["contentColor"]?.jsonPrimitive?.content),
        disabledContainerColor = resolveKetoyColor(colorsObject["disabledContainerColor"]?.jsonPrimitive?.content),
        disabledContentColor = resolveKetoyColor(colorsObject["disabledContentColor"]?.jsonPrimitive?.content)
    )
}

// ─── NavigationBarItem Colours ────────────────────────────────────

/**
 * Parses a JSON object into Material 3 [NavigationBarItemColors].
 *
 * Recognised keys: `selectedIconColor`, `selectedTextColor`, `indicatorColor`,
 * `unselectedIconColor`, `unselectedTextColor`, `disabledIconColor`,
 * `disabledTextColor`.
 *
 * @param colorsObject the JSON descriptor.
 * @return the configured [NavigationBarItemColors].
 */
@Composable
fun parseNavigationBarItemColors(colorsObject: JsonObject): NavigationBarItemColors {
    return NavigationBarItemDefaults.colors(
        selectedIconColor = resolveKetoyColor(colorsObject["selectedIconColor"]?.jsonPrimitive?.content),
        selectedTextColor = resolveKetoyColor(colorsObject["selectedTextColor"]?.jsonPrimitive?.content),
        indicatorColor = resolveKetoyColor(colorsObject["indicatorColor"]?.jsonPrimitive?.content),
        unselectedIconColor = resolveKetoyColor(colorsObject["unselectedIconColor"]?.jsonPrimitive?.content),
        unselectedTextColor = resolveKetoyColor(colorsObject["unselectedTextColor"]?.jsonPrimitive?.content),
        disabledIconColor = resolveKetoyColor(colorsObject["disabledIconColor"]?.jsonPrimitive?.content),
        disabledTextColor = resolveKetoyColor(colorsObject["disabledTextColor"]?.jsonPrimitive?.content)
    )
}

// ─── FAB Elevation ────────────────────────────────────────────────

/**
 * Parses FAB elevation values from a JSON object.
 *
 * Recognised keys (all in dp): `defaultElevation`, `pressedElevation`,
 * `focusedElevation`, `hoveredElevation`.
 *
 * @param elevationObject the JSON descriptor.
 * @return the configured [FloatingActionButtonElevation].
 */
@Composable
fun parseFabElevation(elevationObject: JsonObject): FloatingActionButtonElevation {
    return FloatingActionButtonDefaults.elevation(
        defaultElevation = elevationObject["defaultElevation"]?.jsonPrimitive?.intOrNull?.dp ?: 6.dp,
        pressedElevation = elevationObject["pressedElevation"]?.jsonPrimitive?.intOrNull?.dp ?: 8.dp,
        focusedElevation = elevationObject["focusedElevation"]?.jsonPrimitive?.intOrNull?.dp ?: 8.dp,
        hoveredElevation = elevationObject["hoveredElevation"]?.jsonPrimitive?.intOrNull?.dp ?: 8.dp
    )
}

// ─── FAB Position ─────────────────────────────────────────────────

/**
 * Resolves a FAB position string into a Compose [FabPosition].
 *
 * Recognised values: `"start"`, `"center"`, `"end"`, `"endOverlay"`,
 * `"centerDocked"`, `"endDocked"`.
 *
 * @param position the position string from the JSON payload.
 * @return the corresponding [FabPosition]; defaults to [FabPosition.End].
 */
fun parseFabPosition(position: String?): FabPosition {
    return when (position) {
        "start" -> FabPosition.Start
        "center" -> FabPosition.Center
        "end" -> FabPosition.End
        "endOverlay" -> FabPosition.EndOverlay
        "centerDocked" -> FabPosition.Center
        "endDocked" -> FabPosition.End
        else -> FabPosition.End
    }
}
