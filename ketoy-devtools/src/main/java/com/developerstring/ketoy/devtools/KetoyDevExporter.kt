package com.developerstring.ketoy.devtools

import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.dsl.KUniversalScope
import com.developerstring.ketoy.model.KNode
import java.io.File

/**
 * Base class for exporting Ketoy DSL screens to JSON files.
 *
 * `KetoyDevExporter` bridges the gap between your **Kotlin-first SDUI
 * DSL** and the **JSON files** that the Ketoy Dev Server watches for
 * changes. Subclass it, register your screens in [registerScreens],
 * and then either:
 *
 * - Run the Gradle task `ketoyExport` (which invokes [exportTo] under
 *   the hood), **or**
 * - Call [export] / [exportTo] programmatically from a test or CI
 *   script.
 *
 * ## Usage
 * ```kotlin
 * class MyScreenExporter : KetoyDevExporter() {
 *     override fun registerScreens() {
 *         screen("home") {
 *             KColumn(modifier = KMod(fillMaxSize = true)) {
 *                 KText(text = "Hello World!")
 *             }
 *         }
 *         screen("profile") {
 *             KColumn { KText(text = "Profile Screen") }
 *         }
 *     }
 * }
 *
 * // In a Gradle task or unit test:
 * val exporter = MyScreenExporter()
 * exporter.exportTo(File("ketoy-screens"))
 * ```
 *
 * ## Architecture
 * Each call to [screen] registers a named DSL builder. When
 * [buildAll] runs it executes every builder inside a fresh
 * [KUniversalScope], serialises the resulting [KNode] tree to JSON
 * via `KNode.toJson()`, and returns the map of `name → json`.
 *
 * The exported JSON files are typically placed in the `ketoy-screens/`
 * directory at the project root, which the dev server watches for
 * file-system changes and pushes to connected clients.
 *
 * @see KetoyDevClient
 * @see KetoyDevWrapper
 */
abstract class KetoyDevExporter {

    private val registeredScreens = mutableMapOf<String, KUniversalScope.() -> Unit>()

    /**
     * Override this method to register all screens that should be
     * exported. Call [screen] once per screen:
     *
     * ```kotlin
     * override fun registerScreens() {
     *     screen("home")    { KText(text = "Home") }
     *     screen("profile") { KText(text = "Profile") }
     * }
     * ```
     */
    abstract fun registerScreens()

    /**
     * Register a single screen with its DSL builder lambda.
     *
     * The [builder] is executed lazily when [buildAll] or [export] is
     * called.
     *
     * @param name    Unique screen identifier (e.g. `"home"`, `"settings"`).
     *                This becomes the key in the returned map **and** the
     *                filename (`<name>.json`) when [exportTo] writes to disk.
     * @param builder DSL lambda that constructs the screen’s widget tree.
     */
    fun screen(name: String, builder: KUniversalScope.() -> Unit) {
        registeredScreens[name] = builder
    }

    /**
     * Execute all registered screen builders and serialise their
     * widget trees to JSON.
     *
     * Internally calls [registerScreens] to populate the registry,
     * then runs each builder inside a new [KUniversalScope] and
     * converts the resulting [KNode] to a JSON string.
     *
     * @return A map of `screenName → jsonString`.
     */
    fun buildAll(): Map<String, String> {
        registerScreens()
        return registeredScreens.mapValues { (_, builder) ->
            val scope = KUniversalScope()
            scope.builder()
            val node = if (scope.children.size == 1) {
                scope.children.first()
            } else {
                // Wrap multiple children in a column
                scope.children.first()
            }
            node.toJson()
        }
    }

    /**
     * Export all registered screens as individual JSON files inside
     * [directory]. The directory is created if it does not exist.
     *
     * Each screen is written to `<directory>/<screenName>.json`.
     * Progress and a summary are printed to `stdout`.
     *
     * ```kotlin
     * val exporter = MyScreenExporter()
     * exporter.exportTo(File("ketoy-screens"))
     * // → ketoy-screens/home.json
     * // → ketoy-screens/profile.json
     * ```
     *
     * @param directory Target directory for the JSON files.
     *
     * @see buildAll
     */
    fun exportTo(directory: File) {
        directory.mkdirs()
        val screens = buildAll()
        screens.forEach { (name, json) ->
            val file = File(directory, "$name.json")
            file.writeText(json)
            println("📄 Exported: $name → ${file.absolutePath}")
        }
        println("✅ Exported ${screens.size} screen(s) to ${directory.absolutePath}")
    }

    /**
     * Convenience wrapper that builds all screens and returns the
     * resulting JSON map without writing to disk.
     *
     * Equivalent to calling [buildAll].
     *
     * @return A map of `screenName → jsonString`.
     */
    fun export(): Map<String, String> = buildAll()
}
