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

    /** Define a screen with a string screen name. */
    fun screen(screenName: String, block: ScreenBuilder.() -> Unit) {
        val builder = ScreenBuilder(screenName)
        builder.block()
        builder.build()?.let { screens.add(it) }
    }

    /** Register a pre-built [KetoyScreen]. */
    fun screen(ketoyScreen: KetoyScreen) {
        screens.add(ketoyScreen)
    }
}

/**
 * Builder for configuring a single Ketoy screen.
 */
class ScreenBuilder(private val screenName: String) {

    private var jsonContent: String? = null
    private var dslBuilder: (KUniversalScope.() -> Unit)? = null
    private var composableBuilder: (@androidx.compose.runtime.Composable () -> Unit)? = null
    private var assetPath: String? = null
    private var displayName: String? = null
    private var description: String = ""
    private var version: String = "1.0.0"

    /** Set a human-friendly display name for this screen. */
    fun displayName(name: String) { displayName = name }

    /** Set a description for this screen. */
    fun description(desc: String) { description = desc }

    /** Set a version for this screen. */
    fun version(ver: String) { version = ver }

    /** Define this screen's content from a JSON string. */
    fun fromJson(json: String) {
        jsonContent = json
    }

    /** Define this screen's content via Ketoy DSL. */
    fun dsl(builder: KUniversalScope.() -> Unit) {
        dslBuilder = builder
    }

    /** Define this screen's content from a Composable lambda. */
    fun fromComposable(content: @androidx.compose.runtime.Composable () -> Unit) {
        composableBuilder = content
    }

    /** Define this screen's content from a local asset path. */
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
 */
fun ketoyScreens(block: KetoyScreensScope.() -> Unit): List<KetoyScreen> {
    val scope = KetoyScreensScope()
    scope.block()
    scope.screens.forEach { KetoyScreenRegistry.register(it) }
    return scope.screens
}
