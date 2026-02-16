package com.developerstring.ketoy.devtools

import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.dsl.KUniversalScope
import com.developerstring.ketoy.model.KNode
import java.io.File

/**
 * Utility for exporting Ketoy DSL screens to JSON files.
 * 
 * Developers subclass this and register their screens, then the
 * Gradle task `ketoyExport` runs the exporter to produce JSON files
 * that the dev server watches.
 *
 * Usage:
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
 * ```
 */
abstract class KetoyDevExporter {

    private val registeredScreens = mutableMapOf<String, KUniversalScope.() -> Unit>()

    /**
     * Override this to register your screens.
     */
    abstract fun registerScreens()

    /**
     * Register a screen with its DSL builder.
     */
    fun screen(name: String, builder: KUniversalScope.() -> Unit) {
        registeredScreens[name] = builder
    }

    /**
     * Build all registered screens and return their JSON representations.
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
     * Export all screens to JSON files in the specified directory.
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
     * Export all screens and return the JSON map.
     */
    fun export(): Map<String, String> = buildAll()
}
