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
 * @see KetoyDevClient
 * @see KetoyDevWrapper
 */
abstract class KetoyDevExporter {

    private val registeredScreens = mutableMapOf<String, KUniversalScope.() -> Unit>()

    /**
     * Override this method to register all screens that should be
     * exported. Call [screen] once per screen.
     */
    abstract fun registerScreens()

    /**
     * Register a single screen with its DSL builder lambda.
     *
     * @param name    Unique screen identifier (e.g. `"home"`).
     * @param builder DSL lambda that constructs the screen's widget tree.
     */
    fun screen(name: String, builder: KUniversalScope.() -> Unit) {
        registeredScreens[name] = builder
    }

    /**
     * Execute all registered screen builders and serialise their
     * widget trees to JSON.
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
                scope.children.first()
            }
            node.toJson()
        }
    }

    /**
     * Export all registered screens as individual JSON files inside
     * [directory]. The directory is created if it does not exist.
     *
     * @param directory Target directory for the JSON files.
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
     * @return A map of `screenName → jsonString`.
     */
    fun export(): Map<String, String> = buildAll()
}
