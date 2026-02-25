package com.developerstring.ketoy.cloud

import com.developerstring.ketoy.cloud.cache.KetoyCacheStore

/**
 * Public facade for all Ketoy Cloud operations.
 *
 * `KetoyCloud` is the **primary entry point** for interacting with the Ketoy SDUI backend
 * at runtime. It exposes a clean, coroutine-friendly API that wraps [KetoyCloudService] and
 * [KetoyCloudNavService] so you never need to call those internal services directly.
 *
 * All methods are safe to call from any coroutine scope; suspending functions perform
 * network I/O on [kotlinx.coroutines.Dispatchers.IO] internally.
 *
 * ## Prerequisites
 * Before using any method, ensure the SDK has been initialised:
 * ```kotlin
 * Ketoy.initialize(
 *     cloudConfig = KetoyCloudConfig(
 *         apiKey   = "fa044a28d695b2fa...",
 *         packageName = "com.example.myapp"
 *     )
 * )
 * ```
 *
 * ## Screen cache management
 * ```kotlin
 * // Clear a specific screen's cache
 * KetoyCloud.clearScreenCache("home_screen")
 *
 * // Clear all cached screens
 * KetoyCloud.clearAllCache()
 *
 * // Get all cached screen names
 * val cached: Set<String> = KetoyCloud.getCachedScreenNames()
 *
 * // Check whether a screen is already cached
 * val isCached: Boolean = KetoyCloud.isCached("home_screen")
 *
 * // Get cached version without downloading
 * val version: String? = KetoyCloud.getCachedVersion("home_screen")
 * ```
 *
 * ## Version checking
 * ```kotlin
 * // Check if the server has a newer version (lightweight API call)
 * val needsUpdate: Boolean = KetoyCloud.hasUpdate("home_screen")
 * ```
 *
 * ## Navigation graph operations
 * ```kotlin
 * // Fetch a navigation graph (auto-registers into KetoyCloudNavOverrides)
 * val result = KetoyCloud.fetchNavGraph("nav_main")
 *
 * // Prefetch multiple nav graphs during splash/startup
 * val results = KetoyCloud.prefetchNavGraphs(listOf("nav_main", "nav_settings"))
 *
 * // Check for nav graph updates
 * val hasNav: Boolean = KetoyCloud.hasNavUpdate("nav_main")
 * ```
 *
 * @see KetoyCloudService  Internal service that handles screen fetch orchestration.
 * @see KetoyCloudNavService  Internal service that handles navigation-graph fetch orchestration.
 * @see KetoyCloudConfig  Configuration required before using any cloud method.
 * @see KetoyCacheStore  Low-level cache storage used under the hood.
 */
object KetoyCloud {

    /**
     * Clear the cache for a specific screen.
     *
     * Removes both the metadata (version, timestamp) and the stored JSON
     * content from the local cache. If the screen was not cached, this
     * method returns `false`.
     *
     * ```kotlin
     * val removed = KetoyCloud.clearScreenCache("home_screen")
     * if (removed) Log.d("Cache", "Screen cache cleared")
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return `true` if the screen was found and removed; `false` otherwise.
     * @see clearAllCache
     */
    fun clearScreenCache(screenName: String): Boolean {
        return KetoyCloudService.clearScreenCache(screenName)
    }

    /**
     * Clear all cached screens.
     *
     * Removes every screen entry from [KetoyCacheStore], including both
     * SharedPreferences metadata and internal JSON files.
     *
     * ```kotlin
     * KetoyCloud.clearAllCache()
     * ```
     *
     * @see clearScreenCache
     */
    fun clearAllCache() {
        KetoyCloudService.clearAllCache()
    }

    /**
     * Get the names of all screens currently stored in the local cache.
     *
     * Useful for debugging or displaying an offline-available screen list.
     *
     * ```kotlin
     * val names: Set<String> = KetoyCloud.getCachedScreenNames()
     * names.forEach { Log.d("Cache", "Cached: $it") }
     * ```
     *
     * @return An immutable [Set] of screen identifiers present in the cache.
     * @see isCached
     */
    fun getCachedScreenNames(): Set<String> {
        return KetoyCloudService.getCachedScreenNames()
    }

    /**
     * Check if a screen has an updated version on the server.
     *
     * Uses the lightweight `GET /api/v1/screen/version` endpoint to compare
     * the locally cached version against the server version **without**
     * downloading the full JSON payload. Returns `true` when no local cache
     * exists (i.e. screen has never been fetched).
     *
     * ```kotlin
     * lifecycleScope.launch {
     *     if (KetoyCloud.hasUpdate("home_screen")) {
     *         // Trigger a re-fetch or show an update badge
     *     }
     * }
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return `true` if the server has a newer version or the screen is not cached;
     *         `false` if the cached version matches, or the check fails due to network errors.
     * @see hasNavUpdate
     */
    suspend fun hasUpdate(screenName: String): Boolean {
        return KetoyCloudService.hasUpdate(screenName)
    }

    /**
     * Check if a screen is cached locally.
     *
     * Performs a quick lookup against the [KetoyCacheStore] metadata
     * and file system — no network call is made.
     *
     * ```kotlin
     * if (KetoyCloud.isCached("home_screen")) {
     *     // Safe to use CACHE_ONLY strategy
     * }
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return `true` if both metadata and JSON content exist in the cache.
     * @see getCachedVersion
     */
    fun isCached(screenName: String): Boolean {
        return KetoyCacheStore.isCached(screenName)
    }

    /**
     * Get the cached version string of a screen, or `null` if not cached.
     *
     * This reads only metadata from SharedPreferences — the full JSON
     * content is **not** loaded into memory.
     *
     * ```kotlin
     * val version: String? = KetoyCloud.getCachedVersion("home_screen")
     * Log.d("Cache", "Cached version: $version") // e.g. "1.2.0"
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return The version string (e.g. `"1.2.0"`), or `null` if the screen is not in the cache.
     * @see isCached
     */
    fun getCachedVersion(screenName: String): String? {
        return KetoyCacheStore.getVersion(screenName)
    }

    // ── Navigation Graph Cloud Operations ───────────────────────

    /**
     * Fetch a navigation graph from the cloud.
     *
     * Uses the same cache strategy configured via [KetoyCacheConfig] as screen fetches.
     * On success the parsed [KetoyNavGraph][com.developerstring.ketoy.navigation.KetoyNavGraph]
     * is **automatically registered** into
     * [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides],
     * and any [KetoyNavHost][com.developerstring.ketoy.navigation.KetoyNavHost] observing the
     * matching `navHostName` will recompose immediately.
     *
     * ```kotlin
     * lifecycleScope.launch {
     *     when (val result = KetoyCloud.fetchNavGraph("nav_main")) {
     *         is KetoyCloudNavService.NavFetchResult.Success ->
     *             Log.d("Nav", "Loaded ${result.navHostName} v${result.version}")
     *         is KetoyCloudNavService.NavFetchResult.Error ->
     *             Log.e("Nav", result.message)
     *     }
     * }
     * ```
     *
     * @param navName Cloud screen name for the navigation graph (e.g. `"nav_main"`).
     * @return [KetoyCloudNavService.NavFetchResult] — either [NavFetchResult.Success] with
     *         the parsed graph, or [NavFetchResult.Error] with an error message.
     * @see prefetchNavGraphs
     * @see hasNavUpdate
     */
    suspend fun fetchNavGraph(navName: String): KetoyCloudNavService.NavFetchResult {
        return KetoyCloudNavService.fetchNavGraph(navName)
    }

    /**
     * Prefetch multiple navigation graphs in parallel during app startup.
     *
     * Each graph is fetched concurrently using [coroutineScope] + [async],
     * so all requests run in parallel. Results are returned as a map for
     * per-graph inspection.
     *
     * ```kotlin
     * // In Application.onCreate or a splash-screen ViewModel:
     * lifecycleScope.launch {
     *     val results = KetoyCloud.prefetchNavGraphs(
     *         listOf("nav_main", "nav_settings", "nav_onboarding")
     *     )
     *     results.forEach { (name, result) ->
     *         when (result) {
     *             is KetoyCloudNavService.NavFetchResult.Success ->
     *                 Log.d("Nav", "Prefetched $name v${result.version}")
     *             is KetoyCloudNavService.NavFetchResult.Error ->
     *                 Log.w("Nav", "Failed to prefetch $name: ${result.message}")
     *         }
     *     }
     * }
     * ```
     *
     * @param navNames List of cloud screen names (e.g. `["nav_main", "nav_settings"]`).
     * @return Map of navName to its [KetoyCloudNavService.NavFetchResult].
     * @see fetchNavGraph
     */
    suspend fun prefetchNavGraphs(
        navNames: List<String>
    ): Map<String, KetoyCloudNavService.NavFetchResult> {
        return KetoyCloudNavService.prefetchNavGraphs(navNames)
    }

    /**
     * Check if a navigation graph has an updated version on the server.
     *
     * Uses the lightweight version endpoint — no full JSON download occurs.
     *
     * ```kotlin
     * if (KetoyCloud.hasNavUpdate("nav_main")) {
     *     KetoyCloud.fetchNavGraph("nav_main")
     * }
     * ```
     *
     * @param navName Cloud screen name for the nav graph (e.g. `"nav_main"`).
     * @return `true` if the server reports a different version than the local cache.
     * @see hasUpdate
     */
    suspend fun hasNavUpdate(navName: String): Boolean {
        return KetoyCloudNavService.hasUpdate(navName)
    }

    /**
     * Clear the cache for a specific navigation graph.
     *
     * Removes the cached entry from [KetoyCacheStore] **and** unregisters it
     * from [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides],
     * causing any observing [KetoyNavHost][com.developerstring.ketoy.navigation.KetoyNavHost]
     * to fall back to the next source in the resolution chain.
     *
     * ```kotlin
     * KetoyCloud.clearNavCache("nav_main")
     * ```
     *
     * @param navName Cloud screen name (e.g. `"nav_main"`).
     * @return `true` if cached data was found and removed.
     * @see clearAllNavCache
     */
    fun clearNavCache(navName: String): Boolean {
        return KetoyCloudNavService.clearNavCache(navName)
    }

    /**
     * Clear all cached navigation-graph overrides.
     *
     * Removes all entries from
     * [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides].
     * Does **not** clear the screen cache — only nav graph overrides.
     *
     * ```kotlin
     * KetoyCloud.clearAllNavCache()
     * ```
     *
     * @see clearNavCache
     * @see clearAllCache
     */
    fun clearAllNavCache() {
        KetoyCloudNavService.clearAllNavCache()
    }
}
