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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun parseTopAppBarColors(colorsObject: JsonObject): TopAppBarColors {
    return TopAppBarDefaults.topAppBarColors(
        containerColor = parseColor(colorsObject["containerColor"]?.jsonPrimitive?.content),
        scrolledContainerColor = parseColor(colorsObject["scrolledContainerColor"]?.jsonPrimitive?.content),
        navigationIconContentColor = parseColor(colorsObject["navigationIconContentColor"]?.jsonPrimitive?.content),
        titleContentColor = parseColor(colorsObject["titleContentColor"]?.jsonPrimitive?.content),
        actionIconContentColor = parseColor(colorsObject["actionIconContentColor"]?.jsonPrimitive?.content)
    )
}

// ─── TopAppBar Scroll Behaviour ───────────────────────────────────

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

@Composable
fun parseNavigationDrawerItemColors(colorsObject: JsonObject): NavigationDrawerItemColors {
    return NavigationDrawerItemDefaults.colors(
        selectedContainerColor = parseColor(colorsObject["selectedContainerColor"]?.jsonPrimitive?.content),
        unselectedContainerColor = parseColor(colorsObject["unselectedContainerColor"]?.jsonPrimitive?.content),
        selectedIconColor = parseColor(colorsObject["selectedIconColor"]?.jsonPrimitive?.content),
        unselectedIconColor = parseColor(colorsObject["unselectedIconColor"]?.jsonPrimitive?.content),
        selectedTextColor = parseColor(colorsObject["selectedTextColor"]?.jsonPrimitive?.content),
        unselectedTextColor = parseColor(colorsObject["unselectedTextColor"]?.jsonPrimitive?.content),
        selectedBadgeColor = parseColor(colorsObject["selectedBadgeColor"]?.jsonPrimitive?.content),
        unselectedBadgeColor = parseColor(colorsObject["unselectedBadgeColor"]?.jsonPrimitive?.content)
    )
}

// ─── IconButton Colours ───────────────────────────────────────────

@Composable
fun parseIconButtonColors(colorsObject: JsonObject): IconButtonColors {
    return IconButtonDefaults.iconButtonColors(
        containerColor = parseColor(colorsObject["containerColor"]?.jsonPrimitive?.content),
        contentColor = parseColor(colorsObject["contentColor"]?.jsonPrimitive?.content),
        disabledContainerColor = parseColor(colorsObject["disabledContainerColor"]?.jsonPrimitive?.content),
        disabledContentColor = parseColor(colorsObject["disabledContentColor"]?.jsonPrimitive?.content)
    )
}

// ─── FAB Elevation ────────────────────────────────────────────────

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
