package com.developerstring.ketoy.screen

import androidx.compose.runtime.Composable
import com.developerstring.ketoy.navigation.KetoyRoute
import com.developerstring.ketoy.renderer.JSONStringToUI
import kotlin.reflect.KClass

/**
 * Represents a server-driven screen in the Ketoy framework.
 *
 * A [KetoyScreen] is identified by either:
 * - A **string [routeName]** (for JSON-driven / dynamic screens)
 * - A **type-safe route class** via [routeClass] (Navigation2 `@Serializable`)
 *
 * It can be constructed from:
 * - **JSON string** – the UI tree is parsed and rendered at runtime
 * - **Composable builder** – a native Compose lambda
 * - **Asset path** – loaded from a local JSON asset at render time
 *
 * ## String route examples
 * ```kotlin
 * val homeScreen = KetoyScreen.fromJson(
 *     routeName = "home",
 *     json = """{"type":"Column","children":[...]}"""
 * )
 * val profileScreen = KetoyScreen.fromComposable("profile") { ProfileScreen() }
 * ```
 *
 * ## Type-safe route examples
 * ```kotlin
 * @Serializable data object Home : KetoyRoute
 * @Serializable data class Detail(val id: String) : KetoyRoute
 *
 * val homeScreen = KetoyScreen.fromComposable(Home::class) { HomeScreen() }
 * val detailScreen = KetoyScreen.fromComposable(Detail::class) { DetailScreen() }
 * ```
 */
class KetoyScreen private constructor(
    val routeName: String,
    val routeClass: KClass<*>? = null,
    private val jsonContent: String? = null,
    private val composableBuilder: (@Composable () -> Unit)? = null,
    private val assetPath: String? = null,
    val metadata: ScreenMetadata = ScreenMetadata()
) {

    /**
     * Renders this screen's content.
     */
    @Composable
    fun Content() {
        when {
            composableBuilder != null -> composableBuilder.invoke()
            jsonContent != null -> JSONStringToUI(jsonContent)
            assetPath != null -> AssetScreenContent(assetPath)
            else -> androidx.compose.material3.Text("Empty screen: $routeName")
        }
    }

    companion object {

        // ── String-route factories ──────────────────────────────

        /**
         * Create a screen from a JSON UI tree string.
         *
         * @param routeName Unique route identifier for this screen.
         * @param json      JSON string describing the widget tree.
         * @param metadata  Optional screen metadata.
         */
        fun fromJson(
            routeName: String,
            json: String,
            metadata: ScreenMetadata = ScreenMetadata()
        ) = KetoyScreen(
            routeName = routeName,
            jsonContent = json,
            metadata = metadata
        )

        /**
         * Create a screen from a native Compose builder lambda (string route).
         *
         * @param routeName Unique route identifier.
         * @param metadata  Optional metadata.
         * @param content   Composable lambda that renders the screen.
         */
        fun fromComposable(
            routeName: String,
            metadata: ScreenMetadata = ScreenMetadata(),
            content: @Composable () -> Unit
        ) = KetoyScreen(
            routeName = routeName,
            composableBuilder = content,
            metadata = metadata
        )

        /**
         * Create a screen that loads its JSON content from a local asset.
         *
         * @param routeName Unique route identifier.
         * @param assetPath Path within the Android assets directory.
         * @param metadata  Optional metadata.
         */
        fun fromAsset(
            routeName: String,
            assetPath: String,
            metadata: ScreenMetadata = ScreenMetadata()
        ) = KetoyScreen(
            routeName = routeName,
            assetPath = assetPath,
            metadata = metadata
        )

        // ── Type-safe route factories ───────────────────────────

        /**
         * Create a screen from a `@Serializable` route class and a Compose builder.
         *
         * ```kotlin
         * @Serializable data object Home : KetoyRoute
         * KetoyScreen.fromComposable(Home::class) { HomeScreen() }
         * ```
         *
         * @param T         The route type (a `@Serializable` class implementing [KetoyRoute]).
         * @param routeClass KClass of the route.
         * @param metadata  Optional metadata.
         * @param content   Composable to render.
         */
        fun <T : KetoyRoute> fromComposable(
            routeClass: KClass<T>,
            metadata: ScreenMetadata = ScreenMetadata(),
            content: @Composable () -> Unit
        ) = KetoyScreen(
            routeName = routeClass.simpleName ?: "unknown",
            routeClass = routeClass,
            composableBuilder = content,
            metadata = metadata
        )

        /**
         * Create a screen from a `@Serializable` route class and a JSON string.
         *
         * @param T          The route type.
         * @param routeClass KClass of the route.
         * @param json       JSON string describing the widget tree.
         * @param metadata   Optional metadata.
         */
        fun <T : KetoyRoute> fromJson(
            routeClass: KClass<T>,
            json: String,
            metadata: ScreenMetadata = ScreenMetadata()
        ) = KetoyScreen(
            routeName = routeClass.simpleName ?: "unknown",
            routeClass = routeClass,
            jsonContent = json,
            metadata = metadata
        )

        /**
         * Create a screen from a `@Serializable` route class and an asset path.
         *
         * @param T          The route type.
         * @param routeClass KClass of the route.
         * @param assetPath  Path within the Android assets directory.
         * @param metadata   Optional metadata.
         */
        fun <T : KetoyRoute> fromAsset(
            routeClass: KClass<T>,
            assetPath: String,
            metadata: ScreenMetadata = ScreenMetadata()
        ) = KetoyScreen(
            routeName = routeClass.simpleName ?: "unknown",
            routeClass = routeClass,
            assetPath = assetPath,
            metadata = metadata
        )
    }
}

/**
 * Metadata associated with a [KetoyScreen].
 *
 * Carries optional configuration that can be used for transitions,
 * analytics, or access control.
 *
 * @param title          Human-readable screen title (for app bars, analytics).
 * @param requiresAuth   Whether this screen requires authentication.
 * @param transitionType Transition animation style.
 * @param tags           Arbitrary tags for categorisation or feature flags.
 */
data class ScreenMetadata(
    val title: String = "",
    val requiresAuth: Boolean = false,
    val transitionType: String = "default",
    val tags: List<String> = emptyList()
)

/**
 * Internal composable that loads and renders a JSON screen from assets.
 */
@Composable
private fun AssetScreenContent(assetPath: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val jsonContent = try {
        context.assets.open(assetPath).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        null
    }

    if (jsonContent != null) {
        JSONStringToUI(jsonContent)
    } else {
        androidx.compose.material3.Text("Failed to load asset: $assetPath")
    }
}
