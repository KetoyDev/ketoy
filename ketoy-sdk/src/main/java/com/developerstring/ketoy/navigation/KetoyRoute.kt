package com.developerstring.ketoy.navigation

import kotlinx.serialization.Serializable

/**
 * Marker interface for type-safe Ketoy navigation routes.
 *
 * Developers define their routes as `@Serializable data object` or
 * `@Serializable data class` types that implement this interface,
 * exactly how Navigation Compose 2.8+ handles type-safe navigation.
 *
 * ## Defining routes
 * ```kotlin
 * @Serializable
 * data object Home : KetoyRoute
 *
 * @Serializable
 * data object Profile : KetoyRoute
 *
 * @Serializable
 * data class Detail(val id: String) : KetoyRoute
 *
 * @Serializable
 * data class Product(val productId: Int, val from: String = "home") : KetoyRoute
 * ```
 *
 * ## Using in KetoyNavHost
 * ```kotlin
 * KetoyNavHost(startRoute = Home) {
 *     screen<Home> { HomeScreen() }
 *     screen<Profile> { ProfileScreen() }
 *     screen<Detail> { backStackEntry ->
 *         val detail: Detail = backStackEntry.toRoute()
 *         DetailScreen(id = detail.id)
 *     }
 * }
 * ```
 *
 * ## Navigating
 * ```kotlin
 * val nav = LocalKetoyNavController.current
 * nav.navigate(Detail(id = "42"))
 * nav.navigateAndReplace(Home)
 * nav.popBackStack()
 * ```
 *
 * String-based routes remain supported for JSON-driven / dynamic
 * navigation via [KNavigateAction].
 */
interface KetoyRoute
