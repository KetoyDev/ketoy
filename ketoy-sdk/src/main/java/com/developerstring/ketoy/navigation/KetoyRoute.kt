package com.developerstring.ketoy.navigation

import kotlinx.serialization.Serializable

/**
 * Marker interface for type-safe Ketoy navigation routes.
 *
 * Implement this interface on `@Serializable data object` or
 * `@Serializable data class` types to define type-safe navigation
 * destinations for [KetoyNavHost], following the Navigation Compose 2.8+
 * pattern.
 *
 * Type-safe routes provide compile-time safety for navigation parameters
 * and eliminate stringly-typed route errors. They work alongside
 * string-based routes for a hybrid approach where some screens are
 * defined in code and others are driven by JSON.
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
 * data class Product(
 *     val productId: Int,
 *     val from: String = "home"   // default values supported
 * ) : KetoyRoute
 * ```
 *
 * ## Using in KetoyNavHost
 * ```kotlin
 * KetoyNavHost(startRoute = Home) {
 *     screen<Home> { HomeScreen() }
 *     screen<Profile> { ProfileScreen() }
 *     screen<Detail> { route ->
 *         DetailScreen(id = route.id)
 *     }
 *     screen<Product> { route ->
 *         ProductScreen(productId = route.productId, from = route.from)
 *     }
 * }
 * ```
 *
 * ## Navigating
 * ```kotlin
 * val nav = LocalKetoyNavController.current
 * nav?.navigate(Detail(id = "42"))
 * nav?.navigateAndReplace(Home)
 * nav?.navigateAndClearBackStack(Home)
 * nav?.popBackStack()
 * ```
 *
 * ## Extracting the current route
 * ```kotlin
 * val detail: Detail? = nav?.currentRouteAs<Detail>()
 * ```
 *
 * String-based routes remain fully supported for JSON-driven / dynamic
 * navigation via [KNavigateAction] and [KetoyNavigator].
 *
 * @see KetoyNavHost
 * @see KetoyNavController
 * @see KetoyNavController.navigate
 * @see KetoyNavController.currentRouteAs
 */
interface KetoyRoute
