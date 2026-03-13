package dev.ketoy.gradle.server

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Central in-memory cache for all Ketoy SDUI screen definitions and navigation graphs
 * served by the [KetoyDevServer].
 *
 * ## File conventions
 *
 * | Pattern | Stored in | Key |
 * |---------|-----------|-----|
 * | `home.json` | screens | `"home"` |
 * | `nav_main.json` | navGraphs | `"main"` |
 *
 * All maps are [ConcurrentHashMap] and the version counter is an [AtomicLong], so
 * concurrent reads and writes are safe without external synchronisation.
 *
 * @param watchDir The directory that contains the `.json` screen and `nav_*.json` files.
 */
class ScreenManager(private val watchDir: File) {
    private val screens = ConcurrentHashMap<String, String>()
    private val navGraphs = ConcurrentHashMap<String, String>()
    private val version = AtomicLong(0)

    init {
        loadAllScreens()
    }

    /**
     * Loads (or reloads) **all** `.json` files from the [watchDir].
     *
     * Files whose name starts with `nav_` are treated as navigation graphs;
     * all others are treated as screen definitions.
     */
    fun loadAllScreens() {
        watchDir.listFiles { file -> file.extension == "json" }?.forEach { file ->
            if (file.name.startsWith("nav_")) {
                loadNavGraph(file)
            } else {
                loadScreen(file)
            }
        }
    }

    /**
     * Loads or reloads a single screen JSON file into the cache.
     *
     * @return The JSON string if the content is new or changed, or `null` if unchanged.
     */
    fun loadScreen(file: File): String? {
        return try {
            val json = file.readText().trim()
            val name = file.nameWithoutExtension
            val previous = screens.put(name, json)
            if (previous == json) {
                null
            } else {
                version.incrementAndGet()
                json
            }
        } catch (e: Exception) {
            System.err.println("⚠️  Failed to load ${file.name}: ${e.message}")
            null
        }
    }

    /**
     * Loads or reloads a navigation graph JSON file (`nav_*.json`) into the cache.
     *
     * @return The JSON string if new or changed, or `null` if unchanged.
     */
    fun loadNavGraph(file: File): String? {
        return try {
            val json = file.readText().trim()
            val name = file.nameWithoutExtension.removePrefix("nav_")
            val previous = navGraphs.put(name, json)
            if (previous == json) {
                null
            } else {
                version.incrementAndGet()
                json
            }
        } catch (e: Exception) {
            System.err.println("⚠️  Failed to load nav graph ${file.name}: ${e.message}")
            null
        }
    }

    fun removeScreen(name: String) {
        screens.remove(name)
        version.incrementAndGet()
    }

    fun removeNavGraph(name: String) {
        navGraphs.remove(name)
        version.incrementAndGet()
    }

    fun getScreen(name: String): String? = screens[name]

    fun getNavGraph(name: String): String? = navGraphs[name]

    fun getAllScreens(): Map<String, String> = screens.toMap()

    fun getAllNavGraphs(): Map<String, String> = navGraphs.toMap()

    fun listScreens(): List<String> = screens.keys().toList().sorted()

    fun listNavGraphs(): List<String> = navGraphs.keys().toList().sorted()

    fun getVersion(): Long = version.get()
}
