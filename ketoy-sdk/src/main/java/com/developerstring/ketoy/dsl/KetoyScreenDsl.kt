package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.navigation.KetoyRoute
import com.developerstring.ketoy.screen.KetoyScreen
import com.developerstring.ketoy.screen.KetoyScreenRegistry
import com.developerstring.ketoy.screen.ScreenMetadata
import com.developerstring.ketoy.widget.KetoyActionParser
import com.developerstring.ketoy.widget.KetoyWidgetParser

/**
 * DSL scope for configuring screens during Ketoy initialization.
 *
 * Supports both **string routes** and **type-safe `@Serializable` routes**.
 *
 * ## String routes
 * ```kotlin
 * ketoyScreens {
 *     screen("home") { fromJson(homeJson) }
 *     screen("profile") { fromComposable { ProfileScreen() } }
 * }
 * ```
 *
 * ## Type-safe routes
 * ```kotlin
 * @Serializable data object Home : KetoyRoute
 * @Serializable data class Detail(val id: String) : KetoyRoute
 *
 * ketoyScreens {
 *     screen<Home> { fromComposable { HomeScreen() } }
 *     screen<Detail> { fromComposable { DetailScreen() } }
 * }
 * ```
 */
class KetoyScreensScope {

    @PublishedApi
    internal val screens = mutableListOf<KetoyScreen>()

    /**
     * Define a screen with a string route name.
     */
    fun screen(routeName: String, block: ScreenBuilder.() -> Unit) {
        val builder = ScreenBuilder(routeName)
        builder.block()
        builder.build()?.let { screens.add(it) }
    }

    /**
     * Define a screen with a type-safe `@Serializable` route class.
     *
     * ```kotlin
     * screen<Home> { fromComposable { HomeScreen() } }
     * ```
     */
    inline fun <reified T : KetoyRoute> screen(
        noinline block: TypeSafeScreenBuilder<T>.() -> Unit
    ) {
        val builder = TypeSafeScreenBuilder(T::class)
        builder.block()
        builder.build()?.let { screens.add(it) }
    }

    /**
     * Register a pre-built [KetoyScreen].
     */
    fun screen(ketoyScreen: KetoyScreen) {
        screens.add(ketoyScreen)
    }
}

/**
 * Builder for configuring a single Ketoy screen (string route).
 */
class ScreenBuilder(private val routeName: String) {

    private var jsonContent: String? = null
    private var composableBuilder: (@androidx.compose.runtime.Composable () -> Unit)? = null
    private var assetPath: String? = null
    private var metadata: ScreenMetadata = ScreenMetadata()

    /** Define this screen's content from a JSON string. */
    fun fromJson(json: String) {
        jsonContent = json
    }

    /** Define this screen's content from a Composable lambda. */
    fun fromComposable(content: @androidx.compose.runtime.Composable () -> Unit) {
        composableBuilder = content
    }

    /** Define this screen's content from a local asset path. */
    fun fromAsset(path: String) {
        assetPath = path
    }

    /** Set screen metadata. */
    fun metadata(block: ScreenMetadataBuilder.() -> Unit) {
        val builder = ScreenMetadataBuilder()
        builder.block()
        metadata = builder.build()
    }

    internal fun build(): KetoyScreen? {
        return when {
            jsonContent != null -> KetoyScreen.fromJson(routeName, jsonContent!!, metadata)
            composableBuilder != null -> KetoyScreen.fromComposable(routeName, metadata, composableBuilder!!)
            assetPath != null -> KetoyScreen.fromAsset(routeName, assetPath!!, metadata)
            else -> null
        }
    }
}

/**
 * Builder for configuring a screen with a type-safe `@Serializable` route.
 */
class TypeSafeScreenBuilder<T : KetoyRoute>(
    private val routeClass: kotlin.reflect.KClass<T>
) {

    private var jsonContent: String? = null
    private var composableBuilder: (@androidx.compose.runtime.Composable () -> Unit)? = null
    private var assetPath: String? = null
    private var metadata: ScreenMetadata = ScreenMetadata()

    /** Define this screen's content from a JSON string. */
    fun fromJson(json: String) {
        jsonContent = json
    }

    /** Define this screen's content from a Composable lambda. */
    fun fromComposable(content: @androidx.compose.runtime.Composable () -> Unit) {
        composableBuilder = content
    }

    /** Define this screen's content from a local asset path. */
    fun fromAsset(path: String) {
        assetPath = path
    }

    /** Set screen metadata. */
    fun metadata(block: ScreenMetadataBuilder.() -> Unit) {
        val builder = ScreenMetadataBuilder()
        builder.block()
        metadata = builder.build()
    }

    @PublishedApi
    internal fun build(): KetoyScreen? {
        return when {
            jsonContent != null -> KetoyScreen.fromJson(routeClass, jsonContent!!, metadata)
            composableBuilder != null -> KetoyScreen.fromComposable(routeClass, metadata, composableBuilder!!)
            assetPath != null -> KetoyScreen.fromAsset(routeClass, assetPath!!, metadata)
            else -> null
        }
    }
}

/**
 * Builder for [ScreenMetadata].
 */
class ScreenMetadataBuilder {
    var title: String = ""
    var requiresAuth: Boolean = false
    var transitionType: String = "default"
    var tags: List<String> = emptyList()

    internal fun build() = ScreenMetadata(
        title = title,
        requiresAuth = requiresAuth,
        transitionType = transitionType,
        tags = tags
    )
}

// ── Top-level DSL functions ─────────────────────────────────────

/**
 * DSL entry-point for defining and registering Ketoy screens.
 *
 * ```kotlin
 * ketoyScreens {
 *     screen("home") { fromJson(homeJson) }
 *     screen<Profile> { fromComposable { ProfileScreen() } }
 * }
 * ```
 */
fun ketoyScreens(block: KetoyScreensScope.() -> Unit): List<KetoyScreen> {
    val scope = KetoyScreensScope()
    scope.block()
    scope.screens.forEach { KetoyScreenRegistry.register(it) }
    return scope.screens
}
