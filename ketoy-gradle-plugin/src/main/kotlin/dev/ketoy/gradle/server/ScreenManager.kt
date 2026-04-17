package dev.ketoy.gradle.server

import java.io.File
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Central in-memory cache for all Ketoy SDUI screen definitions and navigation graphs
 * served by the [KetoyDevServer].
 *
 * ## File conventions
 *
 * | Pattern | Stored in | Key | Format |
 * |---------|-----------|-----|--------|
 * | `home.json` | screens | `"home"` | Raw JSON |
 * | `home.ktw`  | screens | `"home"` | Base64-encoded wire bytes |
 * | `nav_main.json` | navGraphs | `"main"` | Raw JSON |
 *
 * `.ktw` files (Ketoy Wire Format) are read as raw bytes, Base64-encoded,
 * and stored with the format tracked in [wireScreens]. The server wraps
 * wire format data as a JSON string value (`"data":"<base64>"`) instead of
 * inline JSON (`"data":{...}`), and adds a `"format":"ktw"` field so the
 * client can detect and decode it.
 *
 * All maps are [ConcurrentHashMap] and the version counter is an [AtomicLong], so
 * concurrent reads and writes are safe without external synchronisation.
 *
 * @param watchDir The directory that contains the screen files (`.json` / `.ktw`) and `nav_*.json` files.
 */
class ScreenManager(private val watchDir: File) {
    private val screens = ConcurrentHashMap<String, String>()
    private val navGraphs = ConcurrentHashMap<String, String>()

    /** Tracks which screen names are wire format (`.ktw`). */
    private val wireScreens = ConcurrentHashMap.newKeySet<String>()

    /** Tracks which nav graph names are wire format (`.ktw`). */
    private val wireNavGraphs = ConcurrentHashMap.newKeySet<String>()

    private val version = AtomicLong(0)

    init {
        loadAllScreens()
    }

    /**
     * Loads (or reloads) **all** `.json` and `.ktw` files from the [watchDir].
     *
     * Files whose name starts with `nav_` are treated as navigation graphs;
     * all others are treated as screen definitions. `.ktw` files take
     * precedence over `.json` files with the same base name.
     */
    fun loadAllScreens() {
        watchDir.listFiles { file ->
            file.extension == "json" || file.extension == "ktw"
        }?.forEach { file ->
            if (file.name.startsWith("nav_")) {
                loadNavGraph(file)
            } else {
                loadScreen(file)
            }
        }
    }

    /**
     * Loads or reloads a single screen file into the cache.
     *
     * For `.json` files, the content is stored as-is (raw JSON text).
     * For `.ktw` files, the content is read as raw bytes, Base64-encoded,
     * and the screen name is tracked in [wireScreens].
     *
     * @return The stored string (JSON or Base64) if the content is new or changed, or `null` if unchanged.
     */
    fun loadScreen(file: File): String? {
        return try {
            val name = file.nameWithoutExtension
            val isWire = file.extension == "ktw"

            val content = if (isWire) {
                val bytes = file.readBytes()
                Base64.getEncoder().encodeToString(bytes)
            } else {
                file.readText().trim()
            }

            val previous = screens.put(name, content)
            if (isWire) wireScreens.add(name) else wireScreens.remove(name)

            if (previous == content) {
                null
            } else {
                version.incrementAndGet()
                content
            }
        } catch (e: Exception) {
            System.err.println("⚠️  Failed to load ${file.name}: ${e.message}")
            null
        }
    }

    /**
     * Loads or reloads a navigation graph file (`nav_*.json` or `nav_*.ktw`) into the cache.
     *
     * For `.json` files, the content is stored as-is (raw JSON text).
     * For `.ktw` files, the content is read as raw bytes, Base64-encoded,
     * and the nav graph name is tracked in [wireNavGraphs].
     *
     * @return The stored string (JSON or Base64) if new or changed, or `null` if unchanged.
     */
    fun loadNavGraph(file: File): String? {
        return try {
            val name = file.nameWithoutExtension.removePrefix("nav_")
            val isWire = file.extension == "ktw"

            val content = if (isWire) {
                val bytes = file.readBytes()
                Base64.getEncoder().encodeToString(bytes)
            } else {
                file.readText().trim()
            }

            val previous = navGraphs.put(name, content)
            if (isWire) wireNavGraphs.add(name) else wireNavGraphs.remove(name)

            if (previous == content) {
                null
            } else {
                version.incrementAndGet()
                content
            }
        } catch (e: Exception) {
            System.err.println("⚠️  Failed to load nav graph ${file.name}: ${e.message}")
            null
        }
    }

    fun removeScreen(name: String) {
        screens.remove(name)
        wireScreens.remove(name)
        version.incrementAndGet()
    }

    fun removeNavGraph(name: String) {
        navGraphs.remove(name)
        wireNavGraphs.remove(name)
        version.incrementAndGet()
    }

    fun getScreen(name: String): String? = screens[name]

    fun getNavGraph(name: String): String? = navGraphs[name]

    fun getAllScreens(): Map<String, String> = screens.toMap()

    fun getAllNavGraphs(): Map<String, String> = navGraphs.toMap()

    fun listScreens(): List<String> = screens.keys().toList().sorted()

    fun listNavGraphs(): List<String> = navGraphs.keys().toList().sorted()

    fun getVersion(): Long = version.get()

    /** Returns `true` if the screen with the given [name] was loaded from a `.ktw` wire format file. */
    fun isWireFormat(name: String): Boolean = wireScreens.contains(name)

    /** Returns the set of screen names that are in wire format. */
    fun getWireScreenNames(): Set<String> = wireScreens.toSet()

    /** Returns `true` if the nav graph with the given [name] was loaded from a `.ktw` wire format file. */
    fun isWireFormatNav(name: String): Boolean = wireNavGraphs.contains(name)

    /** Returns the set of nav graph names that are in wire format. */
    fun getWireNavGraphNames(): Set<String> = wireNavGraphs.toSet()
}
