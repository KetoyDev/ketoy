package com.developerstring.ketoy.devtools

import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.core.toWireBytes
import com.developerstring.ketoy.dsl.KUniversalScope
import com.developerstring.ketoy.model.KNode
import com.developerstring.ketoy.wire.WireFormatConfig
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
     * widget trees to compressed wire bytes.
     *
     * @param config Wire format configuration. Defaults to [WireFormatConfig.OPTIMIZED].
     * @return A map of `screenName → wireBytes`.
     */
    fun buildAllBytes(config: WireFormatConfig = WireFormatConfig.OPTIMIZED): Map<String, ByteArray> {
        registerScreens()
        return registeredScreens.mapValues { (_, builder) ->
            val scope = KUniversalScope()
            scope.builder()
            val node = if (scope.children.size == 1) {
                scope.children.first()
            } else {
                scope.children.first()
            }
            node.toWireBytes(config)
        }
    }

    /**
     * Export all registered screens as individual `.ktw` wire-format
     * files inside [directory]. The directory is created if it does not exist.
     *
     * @param directory Target directory for the wire-format files.
     * @param config    Wire format configuration.
     */
    fun exportBytesTo(directory: File, config: WireFormatConfig = WireFormatConfig.OPTIMIZED) {
        directory.mkdirs()
        val screens = buildAllBytes(config)
        screens.forEach { (name, bytes) ->
            val file = File(directory, "$name.ktw")
            file.writeBytes(bytes)
            println("Exported: $name -> ${file.absolutePath} (${bytes.size} bytes)")
        }
        println("Exported ${screens.size} screen(s) to ${directory.absolutePath}")
    }

    /**
     * Convenience wrapper that builds all screens and returns the
     * resulting wire bytes map without writing to disk.
     *
     * @param config Wire format configuration.
     * @return A map of `screenName → wireBytes`.
     */
    fun exportBytes(config: WireFormatConfig = WireFormatConfig.OPTIMIZED): Map<String, ByteArray> =
        buildAllBytes(config)

    /**
     * Execute all registered screen builders and serialise their
     * widget trees to JSON.
     *
     * @deprecated Use [buildAllBytes] for compressed wire format export.
     */
    @Deprecated(
        message = "Use buildAllBytes() for compressed wire format. Plain JSON export is deprecated.",
        replaceWith = ReplaceWith("buildAllBytes()")
    )
    @Suppress("DEPRECATION")
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
     * Export all registered screens as individual JSON files.
     *
     * @deprecated Use [exportBytesTo] for compressed wire format export.
     */
    @Deprecated(
        message = "Use exportBytesTo() for compressed wire format. Plain JSON export is deprecated.",
        replaceWith = ReplaceWith("exportBytesTo(directory)")
    )
    @Suppress("DEPRECATION")
    fun exportTo(directory: File) {
        directory.mkdirs()
        val screens = buildAll()
        screens.forEach { (name, json) ->
            val file = File(directory, "$name.json")
            file.writeText(json)
            println("Exported: $name -> ${file.absolutePath}")
        }
        println("Exported ${screens.size} screen(s) to ${directory.absolutePath}")
    }

    /**
     * @deprecated Use [exportBytes] for compressed wire format export.
     */
    @Deprecated(
        message = "Use exportBytes() for compressed wire format. Plain JSON export is deprecated.",
        replaceWith = ReplaceWith("exportBytes()")
    )
    @Suppress("DEPRECATION")
    fun export(): Map<String, String> = buildAll()
}
