package com.developerstring.ketoy.devserver

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Central in-memory cache for all Ketoy SDUI screen definitions and navigation graphs
 * served by the [KetoyDevServer].
 *
 * `ScreenManager` owns two concurrent maps — one for screens and one for navigation
 * graphs — and a monotonically increasing version counter. Every mutation (load, reload,
 * remove) bumps the version, which the server exposes to clients so they can detect
 * stale data and request a refresh.
 *
 * ## File conventions
 *
 * | Pattern | Stored in | Key |
 * |---------|-----------|-----|
 * | `home.json` | [screens] | `"home"` |
 * | `nav_main.json` | [navGraphs] | `"main"` |
 *
 * ## Thread safety
 *
 * All maps are [ConcurrentHashMap] and the version counter is an [AtomicLong], so
 * concurrent reads from HTTP handlers and writes from the [FileWatcher] are safe
 * without external synchronisation.
 *
 * ### Usage
 *
 * ```kotlin
 * val manager = ScreenManager(File("./ketoy-screens"))
 * manager.listScreens()        // ["home", "profile"]
 * manager.getScreen("home")     // raw JSON string or null
 * manager.getVersion()          // 3
 * ```
 *
 * @param watchDir The directory that contains the `.json` screen and `nav_*.json` files.
 * @see KetoyDevServer
 * @see FileWatcher
 */
class ScreenManager(private val watchDir: File) {
    /** Screen name → raw JSON content. */
    private val screens = ConcurrentHashMap<String, String>()
    /** Navigation graph name → raw JSON content. */
    private val navGraphs = ConcurrentHashMap<String, String>()
    /**
     * Monotonically increasing version counter. Bumped on every load, reload, or removal.
     * Clients compare their local version against this to detect changes.
     */
    private val version = AtomicLong(0)

    init {
        loadAllScreens()
    }

    /**
     * Loads (or reloads) **all** `.json` files from the [watchDir].
     *
     * Files whose name starts with `nav_` are treated as navigation graphs;
     * all others are treated as screen definitions. Called once during initialisation
     * and again after the initial Gradle export in auto-export mode.
     *
     * @see loadScreen
     * @see loadNavGraph
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
     * If the content is identical to what is already cached, the version is **not** bumped
     * and `null` is returned — callers (e.g., [FileWatcher]) should treat `null` as
     * “no update needed” and skip broadcasting.
     *
     * @param file The `.json` file to read. The file’s name (without extension) becomes
     *   the screen’s logical key.
     * @return The JSON string if the content is new or changed, or `null` if unchanged
     *   or an I/O error occurred.
     * @see loadNavGraph
     */
    fun loadScreen(file: File): String? {
        return try {
            val json = file.readText().trim()
            val name = file.nameWithoutExtension
            val previous = screens.put(name, json)
            if (previous == json) {
                // Content unchanged — skip broadcast
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
     * The `nav_` prefix is stripped from the file name to derive the logical key
     * (e.g., `nav_main.json` → `"main"`). Like [loadScreen], returns `null` when the
     * content has not changed.
     *
     * @param file The `nav_*.json` file to read.
     * @return The JSON string if new or changed, or `null` if unchanged or on error.
     * @see loadScreen
     */
    fun loadNavGraph(file: File): String? {
        return try {
            val json = file.readText().trim()
            // nav_main.json → "main"
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

    /**
     * Removes a screen from the cache (typically when its JSON file is deleted from disk).
     * Bumps the data version so clients know the catalog has changed.
     *
     * @param name The logical screen name (file name without `.json`).
     */
    fun removeScreen(name: String) {
        screens.remove(name)
        version.incrementAndGet()
    }

    /**
     * Removes a navigation graph from the cache (typically when its JSON file is deleted).
     * Bumps the data version so clients know the catalog has changed.
     *
     * @param name The logical nav graph name (e.g., `"main"` for `nav_main.json`).
     */
    fun removeNavGraph(name: String) {
        navGraphs.remove(name)
        version.incrementAndGet()
    }

    /**
     * Returns the raw JSON for a specific screen, or `null` if not cached.
     *
     * @param name The logical screen name.
     * @return The raw JSON string, or `null`.
     */
    fun getScreen(name: String): String? = screens[name]

    /**
     * Returns the raw JSON for a specific navigation graph, or `null` if not cached.
     *
     * @param name The logical nav graph name.
     * @return The raw JSON string, or `null`.
     */
    fun getNavGraph(name: String): String? = navGraphs[name]

    /**
     * Returns a **snapshot** of all cached screens as an immutable map.
     * Keys are screen names; values are raw JSON strings.
     *
     * @return An immutable copy of the screens map.
     */
    fun getAllScreens(): Map<String, String> = screens.toMap()

    /**
     * Returns a **snapshot** of all cached navigation graphs as an immutable map.
     * Keys are nav graph names; values are raw JSON strings.
     *
     * @return An immutable copy of the nav graphs map.
     */
    fun getAllNavGraphs(): Map<String, String> = navGraphs.toMap()

    /**
     * Returns a sorted list of all cached screen names.
     *
     * @return Alphabetically sorted screen name list.
     */
    fun listScreens(): List<String> = screens.keys().toList().sorted()

    /**
     * Returns a sorted list of all cached navigation graph names.
     *
     * @return Alphabetically sorted nav graph name list.
     */
    fun listNavGraphs(): List<String> = navGraphs.keys().toList().sorted()

    /**
     * Returns the current data version — a monotonically increasing counter that is
     * bumped on every mutation. Clients use this value to detect whether their local
     * cache is stale.
     *
     * @return The current version number.
     */
    fun getVersion(): Long = version.get()
}
