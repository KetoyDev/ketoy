package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Scaffold & navigation component properties
// ─────────────────────────────────────────────────────────────

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

@Serializable
data class KNavigationBarProps(
    val modifier: KModifier? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val tonalElevation: Int? = null,
    val windowInsets: KWindowInsets? = null
)

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

@Serializable
data class KSnackBarHostProps(
    val hostState: String? = null,
    val modifier: KModifier? = null,
    val snackbar: List<KNode>? = null
)

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

@Serializable
data class KNavigationRailProps(
    val modifier: KModifier? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val header: List<KNode>? = null,
    val windowInsets: KWindowInsets? = null
)

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

@Serializable
data class KAppBarActionProps(
    val onClick: String? = null,
    val modifier: KModifier? = null,
    val enabled: Boolean? = null,
    val colors: KIconButtonColors? = null,
    val interactionSource: KInteractionSource? = null
)

// ── Supporting data classes ─────────────────────────────────

@Serializable
data class KWindowInsets(
    val left: Int? = null,
    val top: Int? = null,
    val right: Int? = null,
    val bottom: Int? = null,
    val type: String? = null
)

@Serializable
data class KTopAppBarColors(
    val containerColor: String? = null,
    val scrolledContainerColor: String? = null,
    val navigationIconContentColor: String? = null,
    val titleContentColor: String? = null,
    val actionIconContentColor: String? = null
)

@Serializable
data class KTopAppBarScrollBehavior(
    val type: String? = null,
    val canScroll: Boolean? = null
)

@Serializable
data class KBottomAppBarScrollBehavior(
    val type: String? = null,
    val canScroll: Boolean? = null
)

@Serializable
data class KFloatingActionButtonElevation(
    val defaultElevation: Int? = null,
    val pressedElevation: Int? = null,
    val focusedElevation: Int? = null,
    val hoveredElevation: Int? = null,
    val disabledElevation: Int? = null
)

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
