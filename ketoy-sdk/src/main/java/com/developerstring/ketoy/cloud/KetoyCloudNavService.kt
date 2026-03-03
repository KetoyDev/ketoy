package com.developerstring.ketoy.cloud

import android.util.Log
import com.developerstring.ketoy.cloud.cache.KetoyCacheEntry
import com.developerstring.ketoy.cloud.cache.KetoyCacheStore
import com.developerstring.ketoy.cloud.cache.KetoyCacheStrategy
import com.developerstring.ketoy.cloud.network.KetoyApiClient
import com.developerstring.ketoy.cloud.network.KetoyScreenData
import com.developerstring.ketoy.navigation.KetoyCloudNavOverrides
import com.developerstring.ketoy.navigation.KetoyNavGraph
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles fetching and caching navigation graphs from Ketoy Cloud.
 *
 * Uses the **same REST endpoint** as screen fetching:
 * ```
 * GET {baseUrl}/api/v1/screen?screen_name=nav_main
 * ```
 * The `ui` field in the response contains a serialised
 * [KetoyNavGraph][com.developerstring.ketoy.navigation.KetoyNavGraph]
 * instead of a screen-rendering JSON tree.
 *
 * Respects the global cache strategy configured via
 * [Ketoy.initialize][com.developerstring.ketoy.Ketoy.initialize].
 *
 * ## Quick start
 * ```kotlin
 * // Fetch a single nav graph (auto-registers into KetoyCloudNavOverrides)
 * val result = KetoyCloudNavService.fetchNavGraph("nav_main")
 *
 * // Prefetch multiple nav graphs during app startup
 * val results = KetoyCloudNavService.prefetchNavGraphs(
 *     listOf("nav_main", "nav_settings")
 * )
 * ```
 *
 * ## Auto-registration
 * On a successful fetch the parsed [KetoyNavGraph] is **automatically
 * registered** into
 * [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides],
 * which [KetoyNavHost][com.developerstring.ketoy.navigation.KetoyNavHost]
 * observes via Compose snapshot state. Any `KetoyNavHost` that matches
 * the graph’s `navHostName` will recompose instantly.
 *
 * ## API response example
 * ```json
 * {
 *   "success": true,
 *   "data": {
 *     "screenName": "nav_main",
 *     "version": "2.1.0",
 *     "ui": {
 *       "navHostName": "main",
 *       "startDestination": "home",
 *       "destinations": [ ... ]
 *     }
 *   }
 * }
 * ```
 *
 * ## Credentials
 * Uses the existing [KetoyApiClient][com.developerstring.ketoy.cloud.network.KetoyApiClient]
 * initialised by `Ketoy.initialize()`. No base URL or API key is stored
 * here — everything comes from [KetoyCloudConfig].
 *
 * @see KetoyCloud           Public facade that delegates to this service.
 * @see KetoyCloudService    Companion service for screen (non-nav) fetching.
 * @see KetoyCacheStore      Shared local cache storage.
 * @see com.developerstring.ketoy.navigation.KetoyNavGraph
 */
object KetoyCloudNavService {

    /** Log tag for all navigation-graph fetch diagnostics. */
    private const val TAG = "KetoyCloudNav"

    private val backgroundScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            Log.w(TAG, "Background nav fetch failed: ${e.message}")
        }
    )

    /** Tracks nav graphs currently being background-fetched to prevent duplicates. */
    private val backgroundFetchInProgress = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    /** Delegates to the shared cache config from [KetoyCloudService]. */
    private val cacheConfig get() = KetoyCloudService.cacheConfig

    // ── Result type ─────────────────────────────────────────────

    /**
     * Result of a navigation-graph fetch operation.
     *
     * Sealed hierarchy with two outcomes:
     * - [Success] — nav graph loaded and auto-registered.
     * - [Error]   — fetch or parse failed.
     *
     * ```kotlin
     * when (val r = KetoyCloudNavService.fetchNavGraph("nav_main")) {
     *     is NavFetchResult.Success -> Log.d("Nav", "Loaded ${r.navHostName}")
     *     is NavFetchResult.Error   -> Log.e("Nav", r.message, r.cause)
     * }
     * ```
     *
     * @see fetchNavGraph
     */
    sealed class NavFetchResult {
        /**
         * Navigation graph loaded successfully.
         *
         * @property navHostName The `navHostName` extracted from the parsed
         *           [KetoyNavGraph]. This is the identifier that
         *           [KetoyNavHost][com.developerstring.ketoy.navigation.KetoyNavHost]
         *           uses to match cloud overrides.
         * @property version     Version string from the server or local cache
         *                       (e.g. `"2.1.0"`).
         * @property navGraph    The fully parsed [KetoyNavGraph] ready for rendering.
         * @property fromCache   `true` if the graph came from the local cache rather
         *                       than a fresh network response.
         */
        data class Success(
            val navHostName: String,
            val version: String,
            val navGraph: KetoyNavGraph,
            val fromCache: Boolean
        ) : NavFetchResult()

        /**
         * Navigation graph fetch failed.
         *
         * @property navName The cloud screen name that was requested
         *                   (e.g. `"nav_main"`).
         * @property message Human-readable error description suitable for
         *                   logging or display.
         * @property cause   Optional underlying exception (e.g.
         *                   [KetoyNetworkException][com.developerstring.ketoy.cloud.network.KetoyNetworkException]).
         */
        data class Error(
            val navName: String,
            val message: String,
            val cause: Exception? = null
        ) : NavFetchResult()
    }

    // ── Public API ──────────────────────────────────────────────

    /**
     * Fetch a navigation graph from cloud using the configured [KetoyCacheStrategy].
     *
     * The [navName] is the cloud screen name (e.g. `"nav_main"`).
     * The API endpoint is identical to screen fetches:
     * ```
     * GET {baseUrl}/api/v1/screen?screen_name=nav_main
     * ```
     *
     * On success the fetched
     * [KetoyNavGraph][com.developerstring.ketoy.navigation.KetoyNavGraph] is
     * **automatically registered** into
     * [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides].
     * Any [KetoyNavHost][com.developerstring.ketoy.navigation.KetoyNavHost]
     * observing the matching `navHostName` will recompose with the cloud graph.
     *
     * ```kotlin
     * lifecycleScope.launch {
     *     when (val r = KetoyCloudNavService.fetchNavGraph("nav_main")) {
     *         is NavFetchResult.Success ->
     *             Log.d("Nav", "Loaded ${r.navHostName} v${r.version}")
     *         is NavFetchResult.Error ->
     *             Log.e("Nav", r.message)
     *     }
     * }
     * ```
     *
     * @param navName Cloud screen name for the nav graph (e.g. `"nav_main"`).
     * @return [NavFetchResult.Success] with the parsed graph, or
     *         [NavFetchResult.Error] with an error message.
     * @see prefetchNavGraphs
     * @see KetoyCloud.fetchNavGraph
     */
    suspend fun fetchNavGraph(navName: String): NavFetchResult {
        return withContext(Dispatchers.IO) {
            try {
                val cached = KetoyCacheStore.get(navName)
                val isCacheValid = isCacheValid(cached)

                when (cacheConfig.strategy) {
                    KetoyCacheStrategy.NETWORK_FIRST ->
                        handleNetworkFirst(navName, cached)

                    KetoyCacheStrategy.CACHE_FIRST ->
                        handleCacheFirst(navName, cached, isCacheValid)

                    KetoyCacheStrategy.OPTIMISTIC ->
                        handleOptimistic(navName, cached, isCacheValid)

                    KetoyCacheStrategy.CACHE_ONLY ->
                        handleCacheOnly(navName, cached)

                    KetoyCacheStrategy.NETWORK_ONLY ->
                        handleNetworkOnly(navName)
                }
            } catch (e: Exception) {
                NavFetchResult.Error(navName, e.message ?: "Unknown error", e)
            }
        }
    }

    /**
     * Prefetch multiple navigation graphs in parallel.
     *
     * Useful during app initialisation / splash screen to warm the cache
     * for all nav hosts **before**
     * [KetoyNavHost][com.developerstring.ketoy.navigation.KetoyNavHost] composes.
     * Each graph is fetched concurrently via [coroutineScope] + [async].
     *
     * ```kotlin
     * // In Application.onCreate or a splash ViewModel:
     * lifecycleScope.launch {
     *     val results = KetoyCloudNavService.prefetchNavGraphs(
     *         listOf("nav_main", "nav_settings", "nav_onboarding")
     *     )
     *     results.forEach { (name, result) ->
     *         Log.d("Nav", "$name -> $result")
     *     }
     * }
     * ```
     *
     * @param navNames List of cloud screen names
     *                 (e.g. `["nav_main", "nav_settings"]`).
     * @return Map of navName → [NavFetchResult].
     * @see fetchNavGraph
     */
    suspend fun prefetchNavGraphs(
        navNames: List<String>
    ): Map<String, NavFetchResult> = coroutineScope {
        navNames
            .map { name -> async { name to fetchNavGraph(name) } }
            .associate { it.await() }
    }

    /**
     * Check if a navigation graph has an updated version on the server.
     *
     * Uses the lightweight `GET /api/v1/screen/version` endpoint to compare
     * local vs. server versions without downloading the full JSON.
     *
     * ```kotlin
     * if (KetoyCloudNavService.hasUpdate("nav_main")) {
     *     KetoyCloudNavService.fetchNavGraph("nav_main")
     * }
     * ```
     *
     * @param navName Cloud screen name (e.g. `"nav_main"`).
     * @return `true` if the server reports a different version than the
     *         local cache, or if no cache exists.
     * @see KetoyCloud.hasNavUpdate
     */
    suspend fun hasUpdate(navName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cachedVersion = KetoyCacheStore.getVersion(navName)
                    ?: return@withContext true
                val serverVersion = KetoyApiClient.fetchScreenVersion(navName)
                serverVersion.version != cachedVersion
            } catch (e: Exception) {
                Log.w(TAG, "Version check failed for '$navName': ${e.message}")
                false
            }
        }
    }

    /**
     * Clear the cache for a specific navigation graph.
     *
     * Removes the entry from [KetoyCacheStore] **and** unregisters it from
     * [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides],
     * so any observing
     * [KetoyNavHost][com.developerstring.ketoy.navigation.KetoyNavHost]
     * falls back to the next source in its resolution chain.
     *
     * The `navHostName` is derived by stripping the `"nav_"` prefix from
     * [navName] (e.g. `"nav_main"` → `"main"`).
     *
     * @param navName Cloud screen name (e.g. `"nav_main"`).
     * @return `true` if cached data was found and removed.
     * @see clearAllNavCache
     */
    fun clearNavCache(navName: String): Boolean {
        // Remove from Compose-observable overrides
        // navName "nav_main" → navHostName "main"
        val navHostName = navName.removePrefix("nav_")
        KetoyCloudNavOverrides.set(navHostName, null)
        return KetoyCacheStore.remove(navName)
    }

    /**
     * Clear all cached nav-graph overrides.
     *
     * Removes all entries from
     * [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides].
     * Does **not** clear the shared screen cache in [KetoyCacheStore].
     *
     * @see clearNavCache
     */
    fun clearAllNavCache() {
        KetoyCloudNavOverrides.clearAll()
        // Note: KetoyCacheStore.clearAll() would also clear screen caches.
        // We only clear the overrides; cached files remain for screen use.
    }

    // ── Strategy Handlers ───────────────────────────────────────

    /**
     * **NETWORK_FIRST** strategy handler for nav graphs.
     *
     * @param navName Nav-graph cloud name.
     * @param cached  Pre-loaded cache entry (may be `null`).
     * @return [NavFetchResult] with parsed graph or an error.
     */
    private fun handleNetworkFirst(
        navName: String,
        cached: KetoyCacheEntry?
    ): NavFetchResult {
        return try {
            fetchFromNetwork(navName, saveToCache = true)
        } catch (e: Exception) {
            if (cached != null) {
                Log.d(TAG, "Network failed for '$navName', using cache (v${cached.version})")
                parseCachedNavGraph(navName, cached)
            } else {
                NavFetchResult.Error(
                    navName,
                    "Network failed and no cached data available: ${e.message}",
                    e
                )
            }
        }
    }

    /**
     * **CACHE_FIRST** strategy handler for nav graphs.
     *
     * @param navName      Nav-graph cloud name.
     * @param cached        Pre-loaded cache entry (may be `null`).
     * @param isCacheValid `true` if [cached] is within [KetoyCacheConfig.maxAge].
     * @return [NavFetchResult] with parsed graph or an error.
     */
    private fun handleCacheFirst(
        navName: String,
        cached: KetoyCacheEntry?,
        isCacheValid: Boolean
    ): NavFetchResult {
        if (cached != null && isCacheValid) {
            if (cacheConfig.refreshInBackground) {
                fetchInBackground(navName, cachedVersion = cached.version)
            }
            return parseCachedNavGraph(navName, cached)
        }

        return try {
            fetchFromNetwork(navName, saveToCache = true)
        } catch (e: Exception) {
            if (cached != null) {
                Log.d(TAG, "Network failed for '$navName', using stale cache")
                parseCachedNavGraph(navName, cached)
            } else {
                NavFetchResult.Error(navName, "No cache and network failed: ${e.message}", e)
            }
        }
    }

    /**
     * **OPTIMISTIC** (stale-while-revalidate) strategy handler for nav graphs.
     *
     * @param navName      Nav-graph cloud name.
     * @param cached        Pre-loaded cache entry (may be `null`).
     * @param isCacheValid  Unused — optimistic always returns cache.
     * @return [NavFetchResult] with parsed graph or an error.
     */
    private fun handleOptimistic(
        navName: String,
        cached: KetoyCacheEntry?,
        @Suppress("UNUSED_PARAMETER") isCacheValid: Boolean
    ): NavFetchResult {
        if (cached != null) {
            fetchInBackground(navName, cachedVersion = cached.version)
            return parseCachedNavGraph(navName, cached)
        }

        return try {
            fetchFromNetwork(navName, saveToCache = true)
        } catch (e: Exception) {
            NavFetchResult.Error(navName, "No cache and network failed: ${e.message}", e)
        }
    }

    /**
     * **CACHE_ONLY** strategy handler for nav graphs.
     *
     * @param navName Nav-graph cloud name.
     * @param cached   Pre-loaded cache entry (may be `null`).
     * @return [NavFetchResult] with parsed graph or an error.
     */
    private fun handleCacheOnly(
        navName: String,
        cached: KetoyCacheEntry?
    ): NavFetchResult {
        return if (cached != null) {
            parseCachedNavGraph(navName, cached)
        } else {
            NavFetchResult.Error(
                navName,
                "No cached data for '$navName' (cache-only mode)"
            )
        }
    }

    /**
     * **NETWORK_ONLY** strategy handler for nav graphs.
     *
     * @param navName Nav-graph cloud name.
     * @return [NavFetchResult] with fresh graph or an error.
     */
    private fun handleNetworkOnly(navName: String): NavFetchResult {
        return try {
            fetchFromNetwork(navName, saveToCache = false)
        } catch (e: Exception) {
            NavFetchResult.Error(navName, "Network request failed: ${e.message}", e)
        }
    }

    // ── Internal Helpers ────────────────────────────────────────

    /**
     * Fetch a nav graph from the Ketoy API, parse it into a [KetoyNavGraph],
     * and optionally save the raw JSON to the local cache.
     *
     * On success the parsed graph is auto-registered into
     * [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides].
     *
     * @param navName     Cloud screen name.
     * @param saveToCache If `true`, persists via [KetoyCacheStore.put].
     * @return [NavFetchResult.Success] or [NavFetchResult.Error].
     */
    private fun fetchFromNetwork(
        navName: String,
        saveToCache: Boolean
    ): NavFetchResult {
        val data: KetoyScreenData = KetoyApiClient.fetchScreen(navName)
        val uiJsonString = data.ui.toString()

        // Parse the `ui` field as a KetoyNavGraph
        val navGraph = try {
            KetoyNavGraph.fromJson(uiJsonString)
        } catch (e: Exception) {
            return NavFetchResult.Error(
                navName,
                "Failed to parse nav graph from cloud response: ${e.message}",
                e
            )
        }

        if (saveToCache) {
            KetoyCacheStore.put(
                screenName = navName,
                version = data.version,
                jsonContent = uiJsonString
            )
            Log.d(TAG, "Cached nav graph '$navName' v${data.version}")
        }

        // Auto-register into cloud overrides (Compose-observable)
        registerNavGraph(navGraph)

        return NavFetchResult.Success(
            navHostName = navGraph.navHostName,
            version = data.version,
            navGraph = navGraph,
            fromCache = false
        )
    }

    /**
     * Parse a [KetoyCacheEntry] as a [KetoyNavGraph] and register it
     * into [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides].
     *
     * @param navName Cloud screen name.
     * @param cached  The cache entry whose [KetoyCacheEntry.jsonContent]
     *                contains the serialised nav graph.
     * @return [NavFetchResult.Success] or [NavFetchResult.Error] if parsing fails.
     */
    private fun parseCachedNavGraph(
        navName: String,
        cached: KetoyCacheEntry
    ): NavFetchResult {
        val navGraph = try {
            KetoyNavGraph.fromJson(cached.jsonContent)
        } catch (e: Exception) {
            return NavFetchResult.Error(
                navName,
                "Failed to parse cached nav graph: ${e.message}",
                e
            )
        }

        registerNavGraph(navGraph)

        return NavFetchResult.Success(
            navHostName = navGraph.navHostName,
            version = cached.version,
            navGraph = navGraph,
            fromCache = true
        )
    }

    /**
     * Register a [KetoyNavGraph] into
     * [KetoyCloudNavOverrides][com.developerstring.ketoy.navigation.KetoyCloudNavOverrides]
     * so any [KetoyNavHost][com.developerstring.ketoy.navigation.KetoyNavHost]
     * observing the matching `navHostName` recomposes.
     *
     * @param navGraph The fully parsed navigation graph.
     */
    private fun registerNavGraph(navGraph: KetoyNavGraph) {
        KetoyCloudNavOverrides.set(navGraph.navHostName, navGraph)
        Log.d(TAG, "Registered cloud nav graph '${navGraph.navHostName}'")
    }

    /**
     * Launch a background fetch to silently update a cached nav graph.
     *
     * First performs a lightweight version check. If the server reports
     * the same version as [cachedVersion], no further download occurs.
     * A per-graph guard prevents duplicate background fetches.
     *
     * @param navName        Cloud screen name.
     * @param cachedVersion  Current cached version (may be `null`).
     */
    private fun fetchInBackground(navName: String, cachedVersion: String?) {
        if (backgroundFetchInProgress.contains(navName)) return

        backgroundFetchInProgress.add(navName)

        backgroundScope.launch {
            try {
                // Lightweight version check first
                val serverVersion = KetoyApiClient.fetchScreenVersion(navName)
                if (cachedVersion != null && serverVersion.version == cachedVersion) {
                    Log.d(TAG, "Background check: '$navName' is up-to-date (v$cachedVersion)")
                    return@launch
                }

                // Version changed – fetch full nav graph
                val data = KetoyApiClient.fetchScreen(navName)
                val uiJsonString = data.ui.toString()
                KetoyCacheStore.put(navName, data.version, uiJsonString)

                val navGraph = KetoyNavGraph.fromJson(uiJsonString)
                registerNavGraph(navGraph)

                Log.d(TAG, "Background updated '$navName' to v${data.version}")
            } catch (e: Exception) {
                Log.w(TAG, "Background fetch failed for '$navName': ${e.message}")
            } finally {
                backgroundFetchInProgress.remove(navName)
            }
        }
    }

    /**
     * Check whether a cache entry is still valid based on
     * [KetoyCacheConfig.maxAge].
     *
     * @param entry The cache entry to validate (may be `null`).
     * @return `true` if non-null and within the configured max age.
     */
    private fun isCacheValid(entry: KetoyCacheEntry?): Boolean {
        if (entry == null) return false
        val maxAge = cacheConfig.maxAge ?: return true // null = never expires
        val age = System.currentTimeMillis() - entry.cachedAt
        return age < maxAge.inWholeMilliseconds
    }
}
