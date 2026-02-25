package com.developerstring.ketoy.screen

/**
 * **Optional** marker annotation for Ketoy screen composables.
 *
 * This annotation is **not required** for Ketoy to work at runtime.
 * The runtime mechanism is [ProvideKetoyScreen], which creates/retrieves a
 * [KetoyScreen] and provides it via [LocalKetoyScreen]. Use `@KScreen`
 * only when you want external tools (e.g. the Ketoy Dev Tools CLI or
 * IDE plugins) to discover screen functions via reflection.
 *
 * ## Recommended pattern (no annotation needed)
 * ```kotlin
 * @Composable
 * fun HomeScreen() {
 *     ProvideKetoyScreen(screenName = "home") {
 *         KetoyContent(name = "cards", nodeBuilder = { buildCards() })
 *         Text("Native Compose expenses section")
 *         KetoyContent(name = "transactions", nodeBuilder = { buildTransactions() })
 *         Button(onClick = {}) { Text("Compose Button") }
 *     }
 * }
 * ```
 *
 * ## With annotation (optional, for tooling discovery)
 * ```kotlin
 * @KScreen(name = "home")
 * @Composable
 * fun HomeScreen() {
 *     ProvideKetoyScreen(screenName = "home") {
 *         KetoyContent(nodeBuilder = { buildHomeUI() })
 *     }
 * }
 * ```
 *
 * ## How tools use this annotation
 * ```kotlin
 * // Tooling discovers all @KScreen functions via reflection:
 * val screenFunctions = appPackage.declarations
 *     .filter { it.hasAnnotation<KScreen>() }
 * ```
 *
 * @property name The unique screen identifier. Only needed if external
 *                tools scan for `@KScreen` via reflection. Leave empty
 *                to rely on `ProvideKetoyScreen`’s `screenName` parameter.
 * @see ProvideKetoyScreen
 * @see KetoyContent
 * @see KetoyScreen
 * @see KetoyScreenRegistry
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KScreen(val name: String = "")
