package com.developerstring.ketoy.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// ─── Navigation routes ────────────────────────────────────────────

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    data object Analytics : Screen("analytics", "Analytics", Icons.Outlined.Insights, Icons.Filled.Insights)
    data object Cards : Screen("cards", "Cards", Icons.Outlined.CreditCard, Icons.Filled.CreditCard)
    data object Profile : Screen("profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person)
}

val bottomNavScreens = listOf(Screen.Home, Screen.Analytics, Screen.Cards, Screen.Profile)
