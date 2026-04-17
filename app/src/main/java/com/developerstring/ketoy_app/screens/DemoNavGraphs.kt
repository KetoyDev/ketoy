package com.developerstring.ketoy_app.screens

import com.developerstring.ketoy.export.ketoyNavExport
import com.developerstring.ketoy.navigation.KetoyNavAction
import com.developerstring.ketoy.navigation.KetoyNavDestination
import com.developerstring.ketoy.navigation.KetoyNavGraph
import com.developerstring.ketoy.navigation.KetoyNavRegistry
import kotlinx.serialization.Serializable

/**
 * Navigation graph definitions for the entire app.
 *
 * Registered at runtime and exported to JSON by the export test.
 * Single source of truth for destinations **and** navigation actions.
 *
 * ## How live route editing works
 *
 * Each navigation action has an `id` and a `route`:
 * ```json
 * { "id": "go_favorites", "route": "favorites", "label": "Open Favorites" }
 * ```
 *
 * In code you simply use `nav?.navigateToRoute("go_favorites")`.
 * The route is defined **once** here (and in the exported JSON).
 *
 * When the dev server pushes an updated JSON where:
 * ```json
 * { "id": "go_favorites", "route": "bookmarks" }
 * ```
 * …the same code now navigates to **"bookmarks"** instead. No recompile needed.
 */

object AppNavGraphs {

    // ── Main app nav graph (bottom bar + drawer) ────────────────

    val main = KetoyNavGraph(
        navHostName = "main",
        startRoute = "home",
        destinations = listOf(
            KetoyNavDestination(
                id = "home", route = "home", screenName = "home",
                label = "Home", icon = "home", selectedIcon = "home",
                isStartDestination = true
            ),
            KetoyNavDestination(
                id = "analytics", route = "analytics", screenName = "analytics",
                label = "Analytics", icon = "insights", selectedIcon = "insights"
            ),
            KetoyNavDestination(
                id = "cards", route = "cards", screenName = "cards",
                label = "Cards", icon = "credit_card", selectedIcon = "credit_card"
            ),
            KetoyNavDestination(
                id = "history", route = "history", screenName = "historyScreen",
                label = "History", icon = "schedule", selectedIcon = "schedule"
            ),
            KetoyNavDestination(
                id = "profile", route = "profile", screenName = "profile",
                label = "Profile", icon = "person", selectedIcon = "person"
            ),
            KetoyNavDestination(
                id = "demo_nav", route = "demo_nav", screenName = "demo_nav",
                label = "Nav Demo", icon = "navigation", selectedIcon = "navigation"
            ),
        ),
        navigations = listOf(
            KetoyNavAction(id = "go_home", route = "home", label = "Go Home"),
            KetoyNavAction(id = "go_analytics", route = "analytics", label = "Go Analytics"),
            KetoyNavAction(id = "go_cards", route = "cards", label = "Go Cards"),
            KetoyNavAction(id = "go_history", route = "history", label = "Go History"),
            KetoyNavAction(id = "go_profile", route = "profile", label = "Go Profile"),
            KetoyNavAction(id = "go_demo_nav", route = "demo_nav", label = "Open Nav Demo"),
        )
    )

    // ── Demo nav graph (nested screen-to-screen navigation) ─────

    val demo = KetoyNavGraph(
        navHostName = "demo",
        startRoute = "explore",
        destinations = listOf(
            KetoyNavDestination(
                id = "explore", route = "explore", screenName = "explore",
                label = "Explore", icon = "explore", selectedIcon = "explore",
                isStartDestination = true
            ),
            KetoyNavDestination(
                id = "favorites", route = "favorites", screenName = "favorites",
                label = "Favorites", icon = "favorite_border", selectedIcon = "favorite"
            ),
            KetoyNavDestination(
                id = "notifications", route = "notifications", screenName = "notifications",
                label = "Notifications", icon = "notifications_none", selectedIcon = "notifications"
            ),
            KetoyNavDestination(
                id = "settings", route = "settings", screenName = "settings",
                label = "Settings", icon = "settings", selectedIcon = "settings"
            ),
        ),
        navigations = listOf(
            KetoyNavAction(id = "go_favorites", route = "favorites", label = "Open Favorites"),
            KetoyNavAction(id = "go_notifications", route = "notifications", label = "Open Notifications"),
            KetoyNavAction(id = "go_settings", route = "settings", label = "Open Settings"),
        )
    )

    /** Register all nav graphs with [KetoyNavRegistry]. */
    fun registerAll() {
        KetoyNavRegistry.register(main)
        KetoyNavRegistry.register(demo)
    }
}

// ── Auto-export registrations ───────────────────────────────
// These register the nav graphs with the export system automatically.
val mainNavExport = ketoyNavExport(AppNavGraphs.main)
val demoNavExport = ketoyNavExport(AppNavGraphs.demo)
