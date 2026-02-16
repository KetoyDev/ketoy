package com.developerstring.ketoy.cloud.cache

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Local cache storage for Ketoy server-driven screens.
 *
 * Uses a two-tier approach:
 * - **SharedPreferences** for metadata (version, timestamp) – fast lookups
 * - **Internal files** for the actual JSON content – handles large payloads
 *   without hitting SharedPreferences size limits
 */
object KetoyCacheStore {

    private const val PREFS_NAME = "ketoy_screen_cache"
    private const val CACHE_DIR_NAME = "ketoy_cache"
    private const val META_PREFIX = "meta_"

    private var prefs: SharedPreferences? = null
    private var cacheDir: File? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ── Initialization ──────────────────────────────────────────

    /**
     * Initialize the cache store with an Android context.
     * Must be called before any cache operations.
     */
    fun initialize(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cacheDir = File(context.applicationContext.filesDir, CACHE_DIR_NAME).also {
            if (!it.exists()) it.mkdirs()
        }
    }

    // ── Write ───────────────────────────────────────────────────

    /**
     * Save a screen's JSON content and metadata to cache.
     *
     * @param screenName The screen identifier.
     * @param version    The version string from the server.
     * @param jsonContent The full JSON UI tree.
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
     * Get a cached screen entry, or null if not cached.
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
     * Get only the cached version string for a screen, without loading content.
     * Useful for quick version comparison.
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
     * Get the cached-at timestamp for a screen.
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
     * Check if a screen is cached.
     */
    fun isCached(screenName: String): Boolean {
        return prefs?.contains("${META_PREFIX}$screenName") == true
                && getScreenFile(screenName)?.exists() == true
    }

    // ── Delete ──────────────────────────────────────────────────

    /**
     * Remove a specific screen from cache.
     *
     * @return true if the screen was found and removed.
     */
    fun remove(screenName: String): Boolean {
        val existed = prefs?.contains("${META_PREFIX}$screenName") == true
        prefs?.edit()?.remove("${META_PREFIX}$screenName")?.apply()
        getScreenFile(screenName)?.delete()
        return existed
    }

    /**
     * Clear all cached screens.
     */
    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
        cacheDir?.listFiles()?.forEach { it.delete() }
    }

    /**
     * Get all cached screen names.
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
