package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.screen.KetoyScreen
import com.developerstring.ketoy.screen.KetoyScreenRegistry

/**
 * DSL scope for configuring screens during Ketoy initialization.
 *
 * ```kotlin
 * ketoyScreens {
 *     screen("home") {
 *         displayName("Home")
 *         fromJson(homeJson)
 *     }
 *     screen("profile") { dsl { KColumn { KText("Profile") } } }
 * }
 * ```
 */
class KetoyScreensScope {

    @PublishedApi
    internal val screens = mutableListOf<KetoyScreen>()

    /**
     * Defines a screen with the given [screenName] and configures it via a [ScreenBuilder].
     *
     * ```kotlin
     * screen("home") {
     *     displayName("Home")
     *     description("Main landing screen")
     *     fromJson(homeJson)
     * }
     * ```
     *
     * @param screenName Unique identifier for this screen (used for navigation and lookup).
     * @param block      Configuration lambda applied to a [ScreenBuilder] receiver.
     */
    fun screen(screenName: String, block: ScreenBuilder.() -> Unit) {
        val builder = ScreenBuilder(screenName)
        builder.block()
        builder.build()?.let { screens.add(it) }
    }

    /**
     * Registers a pre-built [KetoyScreen] instance directly.
     *
     * Use this overload when you already have a fully-constructed screen
     * object (e.g. decoded from a remote config or created elsewhere).
     *
     * @param ketoyScreen The pre-built screen to register.
     */
    fun screen(ketoyScreen: KetoyScreen) {
        screens.add(ketoyScreen)
    }
}

/**
 * Builder for configuring a single Ketoy screen.
 *
 * Created automatically by [KetoyScreensScope.screen] and provides a fluent
 * API to set metadata (display name, description, version) and content source
 * (JSON, DSL, Composable, or asset).
 *
 * Only **one** content source should be specified. If multiple are set, the priority
 * order is: [fromJson] > [fromComposable] > [fromAsset] > [dsl].
 *
 * @param screenName Unique identifier for the screen.
 */
class ScreenBuilder(private val screenName: String) {

    private var jsonContent: String? = null
    private var dslBuilder: (KUniversalScope.() -> Unit)? = null
    private var composableBuilder: (@androidx.compose.runtime.Composable () -> Unit)? = null
    private var assetPath: String? = null
    private var displayName: String? = null
    private var description: String = ""
    private var version: String = "1.0.0"

    /**
     * Sets a human-friendly display name for this screen.
     *
     * If not provided, the screen name is used with underscores replaced by
     * spaces and the first character capitalised.
     *
     * @param name The display name shown in dev-tools and screen catalogues.
     */
    fun displayName(name: String) { displayName = name }

    /**
     * Sets a short description for this screen (shown in dev-tools).
     *
     * @param desc A brief description of the screen’s purpose.
     */
    fun description(desc: String) { description = desc }

    /**
     * Sets the version string for this screen (defaults to `"1.0.0"`).
     *
     * @param ver SemVer-style version, e.g. `"2.1.0"`.
     */
    fun version(ver: String) { version = ver }

    /**
     * Defines this screen’s content from a raw JSON string.
     *
     * The JSON is parsed into a [KNode][com.developerstring.ketoy.model.KNode] tree at registration time.
     *
     * @param json A valid Ketoy screen JSON payload.
     */
    fun fromJson(json: String) {
        jsonContent = json
    }

    /**
     * Defines this screen’s content via the Ketoy DSL.
     *
     * ```kotlin
     * dsl {
     *     KColumn {
     *         KText("Built with DSL")
     *     }
     * }
     * ```
     *
     * @param builder Lambda with [KUniversalScope] receiver to build the screen UI.
     */
    fun dsl(builder: KUniversalScope.() -> Unit) {
        dslBuilder = builder
    }

    /**
     * Defines this screen’s content from a native Composable lambda.
     *
     * Use this when you need full Compose interop for a specific screen
     * rather than server-driven nodes.
     *
     * @param content A `@Composable` lambda that renders the screen.
     */
    fun fromComposable(content: @androidx.compose.runtime.Composable () -> Unit) {
        composableBuilder = content
    }

    /**
     * Defines this screen’s content from a local asset file path.
     *
     * The asset is loaded and parsed into a node tree at registration time.
     *
     * @param path Relative path inside the app’s assets folder (e.g. `"screens/home.json"`).
     */
    fun fromAsset(path: String) {
        assetPath = path
    }

    internal fun build(): KetoyScreen? {
        val dn = displayName ?: screenName.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
        return when {
            jsonContent != null -> KetoyScreen.fromJson(screenName, jsonContent!!, displayName = dn, description = description, version = version)
            composableBuilder != null -> KetoyScreen.fromComposable(screenName, displayName = dn, description = description, version = version, composable = composableBuilder!!)
            assetPath != null -> KetoyScreen.fromAsset(screenName, assetPath!!, displayName = dn, description = description, version = version)
            dslBuilder != null -> KetoyScreen.create(screenName, displayName = dn, description = description, version = version, dslBuilder = dslBuilder!!)
            else -> null
        }
    }
}

/**
 * DSL entry-point for defining and registering Ketoy screens.
 *
 * All screens declared inside the [block] are automatically registered in
 * [KetoyScreenRegistry] and returned as a list for additional inspection.
 *
 * ```kotlin
 * ketoyScreens {
 *     screen("home") {
 *         displayName("Home")
 *         description("Main landing screen")
 *         fromJson(homeJson)
 *     }
 *     screen("profile") { dsl { KColumn { KText("Profile") } } }
 * }
 * ```
 *
 * @param block Lambda with [KetoyScreensScope] receiver to define screens.
 * @return The list of [KetoyScreen] instances that were registered.
 *
 * @see KetoyScreensScope
 * @see ScreenBuilder
 */
fun ketoyScreens(block: KetoyScreensScope.() -> Unit): List<KetoyScreen> {
    val scope = KetoyScreensScope()
    scope.block()
    scope.screens.forEach { KetoyScreenRegistry.register(it) }
    return scope.screens
}
