package com.developerstring.ketoy.screen

/**
 * Marks a `@Composable` function as a Ketoy server-driven screen.
 *
 * The [name] parameter identifies the screen for cloud lookup,
 * dev-server hot reload, caching, and JSON export. Inside the
 * annotated function you use one or more [KetoyContent] composables
 * to define the screen's content sections.
 *
 * A single `@KScreen` can contain **multiple** [KetoyContent] blocks,
 * each identified by a `contentId`. When exported, all content
 * blocks are written into a single JSON file.
 *
 * ## Example — single content
 * ```kotlin
 * @KScreen(name = "home")
 * @Composable
 * fun HomeScreen() {
 *     KetoyContent(nodeBuilder = { buildHomeUI() })
 * }
 * ```
 *
 * ## Example — multiple contents
 * ```kotlin
 * @KScreen(name = "dashboard")
 * @Composable
 * fun DashboardScreen() {
 *     KetoyContent(contentId = "header", nodeBuilder = { buildHeader() })
 *     KetoyContent(contentId = "body",   nodeBuilder = { buildBody() })
 *     KetoyContent(contentId = "footer", nodeBuilder = { buildFooter() })
 * }
 * ```
 *
 * @param name The unique screen identifier used for routing, cloud lookup,
 *             and JSON export. Must match the dev-server filename
 *             (e.g. `"home"` → `home.json`).
 *
 * @see KetoyContent
 * @see KetoyScreen
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KScreen(val name: String = "")
