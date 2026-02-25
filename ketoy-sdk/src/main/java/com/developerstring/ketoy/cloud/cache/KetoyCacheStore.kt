package com.developerstring.ketoy.cloud.cache

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Local cache storage for Ketoy server-driven screens and navigation graphs.
 *
 * Uses a **two-tier** approach for efficiency and robustness:
 *
 * | Tier                | Stores                              | Reason                                |
 * |---------------------|--------------------------------------|---------------------------------------|
 * | `SharedPreferences` | Metadata (version, timestamp)        | Fast key-value lookups                |
 * | Internal files      | Actual JSON content (`*.json`)       | Handles large payloads without        |
 * |                     |                                      | hitting SharedPreferences size limits |
 *
 * ## Lifecycle
 * [initialize] must be called with an Android [Context] before any
 * cache operations. This is handled automatically by
 * [Ketoy.initialize][com.developerstring.ketoy.Ketoy.initialize].
 *
 * ## Thread safety
 * Individual reads and writes are atomic at the SharedPreferences
 * level; file I/O runs on the caller’s dispatcher (typically
 * [Dispatchers.IO][kotlinx.coroutines.Dispatchers.IO]).
 *
 * ## Example
 * ```kotlin
 * // Typically called once during SDK init:
 * KetoyCacheStore.initialize(applicationContext)
 *
 * // Write
 * KetoyCacheStore.put("home_screen", "1.0.0", homeJson)
 *
 * // Read
 * val entry: KetoyCacheEntry? = KetoyCacheStore.get("home_screen")
 *
 * // Quick version check
 * val version: String? = KetoyCacheStore.getVersion("home_screen")
 *
 * // Delete
 * KetoyCacheStore.remove("home_screen")
 * KetoyCacheStore.clearAll()
 * ```
 *
 * @see KetoyCacheEntry
 * @see KetoyCacheConfig
 * @see com.developerstring.ketoy.cloud.KetoyCloudService
 */
object KetoyCacheStore {

    /** SharedPreferences file name for cache metadata. */
    private const val PREFS_NAME = "ketoy_screen_cache"
    /** Internal directory name for cached JSON files. */
    private const val CACHE_DIR_NAME = "ketoy_cache"
    /** Key prefix for metadata entries in SharedPreferences. */
    private const val META_PREFIX = "meta_"

    private var prefs: SharedPreferences? = null
    private var cacheDir: File? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ── Initialization ──────────────────────────────────────────

    /**
     * Initialise the cache store with an Android [Context].
     *
     * Must be called **before** any cache operations. This is handled
     * automatically by
     * [Ketoy.initialize][com.developerstring.ketoy.Ketoy.initialize].
     *
     * ```kotlin
     * KetoyCacheStore.initialize(applicationContext)
     * ```
     *
     * @param context Any Android [Context]; the application context is
     *                extracted automatically.
     */
    fun initialize(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cacheDir = File(context.applicationContext.filesDir, CACHE_DIR_NAME).also {
            if (!it.exists()) it.mkdirs()
        }
    }

    // ── Write ───────────────────────────────────────────────────

    /**
     * Save a screen’s JSON content and metadata to the local cache.
     *
     * - Metadata (version, timestamp) is stored in `SharedPreferences`.
     * - The raw JSON string is written to an internal file named
     *   `{screenName}.json` inside the `ketoy_cache` directory.
     *
     * ```kotlin
     * KetoyCacheStore.put("home_screen", "1.0.0", homeUiJson)
     * ```
     *
     * @param screenName  The screen identifier (e.g. `"home_screen"`).
     * @param version     The version string from the server (e.g. `"1.0.0"`).
     * @param jsonContent The full JSON UI tree as a raw string.
     */
    fun put(screenName: String, version: String, jsonContent: String) {
        val entry = KetoyCacheEntry(
            screenName = screenName,
            version = version,
            jsonContent = "", // metadata only in prefs
            cachedAt = System.currentTimeMillis()
        )

        // Store metadata in SharedPreferences
        prefs?.edit()
            ?.putString("${META_PREFIX}$screenName", json.encodeToString(entry))
            ?.apply()

        // Store actual JSON in internal file (handles large payloads)
        getScreenFile(screenName)?.writeText(jsonContent)
    }

    // ── Read ────────────────────────────────────────────────────

    /**
     * Get a cached screen entry, or `null` if not cached.
     *
     * Loads metadata from SharedPreferences and the JSON content from
     * the internal file. Returns `null` if either is missing or corrupted.
     *
     * ```kotlin
     * val entry: KetoyCacheEntry? = KetoyCacheStore.get("home_screen")
     * entry?.let { Log.d("Cache", "version=${it.version}") }
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return A fully populated [KetoyCacheEntry], or `null`.
     */
    fun get(screenName: String): KetoyCacheEntry? {
        val metaJson = prefs?.getString("${META_PREFIX}$screenName", null) ?: return null
        val meta = try {
            json.decodeFromString<KetoyCacheEntry>(metaJson)
        } catch (e: Exception) {
            null
        } ?: return null

        val content = getScreenFile(screenName)?.takeIf { it.exists() }?.readText()
            ?: return null

        return meta.copy(jsonContent = content)
    }

    /**
     * Get only the cached version string for a screen, without loading
     * the (potentially large) JSON content into memory.
     *
     * Useful for quick version comparison before deciding whether to
     * download fresh data.
     *
     * ```kotlin
     * val version: String? = KetoyCacheStore.getVersion("home_screen")
     * ```
     *
     * @param screenName The screen identifier.
     * @return The version string (e.g. `"1.0.0"`), or `null` if not cached.
     * @see get
     */
    fun getVersion(screenName: String): String? {
        val metaJson = prefs?.getString("${META_PREFIX}$screenName", null) ?: return null
        return try {
            json.decodeFromString<KetoyCacheEntry>(metaJson).version
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get the epoch-millis timestamp of when a screen was cached.
     *
     * @param screenName The screen identifier.
     * @return Epoch millis, or `null` if not cached.
     * @see KetoyCacheEntry.cachedAt
     */
    fun getCachedAt(screenName: String): Long? {
        val metaJson = prefs?.getString("${META_PREFIX}$screenName", null) ?: return null
        return try {
            json.decodeFromString<KetoyCacheEntry>(metaJson).cachedAt
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if a screen is cached (both metadata in SharedPreferences
     * and a JSON file on disk).
     *
     * @param screenName The screen identifier.
     * @return `true` if metadata and content file both exist.
     * @see get
     */
    fun isCached(screenName: String): Boolean {
        return prefs?.contains("${META_PREFIX}$screenName") == true
                && getScreenFile(screenName)?.exists() == true
    }

    // ── Delete ──────────────────────────────────────────────────

    /**
     * Remove a specific screen from the cache.
     *
     * Deletes both the SharedPreferences metadata and the internal
     * JSON file.
     *
     * @param screenName The screen identifier.
     * @return `true` if the screen was found and removed.
     * @see clearAll
     */
    fun remove(screenName: String): Boolean {
        val existed = prefs?.contains("${META_PREFIX}$screenName") == true
        prefs?.edit()?.remove("${META_PREFIX}$screenName")?.apply()
        getScreenFile(screenName)?.delete()
        return existed
    }

    /**
     * Clear all cached screens.
     *
     * Wipes all SharedPreferences metadata and deletes every file in
     * the cache directory.
     *
     * @see remove
     */
    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
        cacheDir?.listFiles()?.forEach { it.delete() }
    }

    /**
     * Get the names of all screens currently in the cache.
     *
     * Strips the internal [META_PREFIX] from SharedPreferences keys.
     *
     * ```kotlin
     * val names: Set<String> = KetoyCacheStore.getAllCachedScreenNames()
     * ```
     *
     * @return An immutable [Set] of screen identifiers.
     */
    fun getAllCachedScreenNames(): Set<String> {
        return prefs?.all?.keys
            ?.filter { it.startsWith(META_PREFIX) }
            ?.map { it.removePrefix(META_PREFIX) }
            ?.toSet()
            ?: emptySet()
    }

    // ── Internal ────────────────────────────────────────────────

    private fun getScreenFile(screenName: String): File? {
        return cacheDir?.let { File(it, "${screenName}.json") }
    }
}
