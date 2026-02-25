package com.developerstring.ketoy_app.navigation

import com.developerstring.ketoy.navigation.KetoyRoute
import com.developerstring.ketoy.util.KIconRef
import com.developerstring.ketoy.util.KIcons
import kotlinx.serialization.Serializable

// ─── Type-safe @Serializable navigation routes ──────────────────

@Serializable
data object HomeRoute : KetoyRoute

@Serializable
data object AnalyticsRoute : KetoyRoute

@Serializable
data object CardsRoute : KetoyRoute

@Serializable
data object ProfileRoute : KetoyRoute

@Serializable
data object HistoryRoute : KetoyRoute

@Serializable
data object DemoNavRoute : KetoyRoute

// ─── Bottom nav item descriptor (UI metadata only) ──────────────

data class BottomNavItem<T : KetoyRoute>(
    val route: T,
    val label: String,
    val icon: KIconRef,
    val selectedIcon: KIconRef,
)

val bottomNavItems = listOf(
    BottomNavItem(HomeRoute, "Home", KIcons.Outlined.Home, KIcons.Filled.Home),
    BottomNavItem(AnalyticsRoute, "Analytics", KIcons.Outlined.Insights, KIcons.Filled.Insights),
    BottomNavItem(CardsRoute, "Cards", KIcons.Outlined.CreditCard, KIcons.Filled.CreditCard),
    BottomNavItem(HistoryRoute, "History", KIcons.Outlined.Schedule, KIcons.Filled.Schedule),
    BottomNavItem(ProfileRoute, "Profile", KIcons.Outlined.Person, KIcons.Filled.Person),
)
