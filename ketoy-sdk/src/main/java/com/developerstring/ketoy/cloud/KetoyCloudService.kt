package com.developerstring.ketoy.cloud

import android.util.Log
import com.developerstring.ketoy.cloud.cache.KetoyCacheConfig
import com.developerstring.ketoy.cloud.cache.KetoyCacheEntry
import com.developerstring.ketoy.cloud.cache.KetoyCacheStore
import com.developerstring.ketoy.cloud.cache.KetoyCacheStrategy
import com.developerstring.ketoy.cloud.network.KetoyApiClient
import com.developerstring.ketoy.cloud.network.KetoyNetworkException
import com.developerstring.ketoy.cloud.network.KetoyScreenData
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Orchestrates screen fetching from Ketoy Cloud with intelligent caching.
 *
 * This is the internal engine behind [KetoyCloud] and [KetoyCloudScreen].
 * It implements all five cache strategies defined by [KetoyCacheStrategy]:
 *
 * | Strategy          | Behaviour                                                |
 * |-------------------|----------------------------------------------------------|
 * | `NETWORK_FIRST`   | Try network, fall back to cache on failure               |
 * | `CACHE_FIRST`     | Use valid cache, fall back to network when stale/missing |
 * | `OPTIMISTIC`      | Return cache instantly, refresh in background (SWR)      |
 * | `CACHE_ONLY`      | Only use cache, never make network requests              |
 * | `NETWORK_ONLY`    | Only use network, never use or update cache              |
 *
 * ## Typical call flow (internal — called by composables)
 * ```kotlin
 * val result: FetchResult = KetoyCloudService.fetchScreen("home_screen")
 * when (result) {
 *     is FetchResult.Success -> renderUi(result.uiJson)
 *     is FetchResult.Error   -> showError(result.message)
 * }
 * ```
 *
 * ## Background refresh
 * When [KetoyCacheConfig.refreshInBackground] is enabled, the `CACHE_FIRST`
 * and `OPTIMISTIC` strategies silently fetch fresh data after returning
 * cached content. A per-screen guard ([backgroundFetchInProgress]) prevents
 * duplicate background fetches for the same screen.
 *
 * ## Thread safety
 * All public suspend functions switch to [kotlinx.coroutines.Dispatchers.IO]
 * internally.  Background refreshes run in a dedicated [CoroutineScope]
 * with a [SupervisorJob] so a single failure does not cancel siblings.
 *
 * @see KetoyCloud            Public facade that delegates to this service.
 * @see KetoyCacheStore        Local cache storage.
 * @see KetoyCacheConfig       Cache configuration (strategy, max age, background refresh).
 * @see KetoyApiClient         Low-level HTTP client used for network calls.
 */
object KetoyCloudService {

    /** Log tag used for all cache and fetch diagnostics. */
    private const val TAG = "KetoyCloud"

    /**
     * Active cache configuration.
     *
     * Set during [com.developerstring.ketoy.Ketoy.initialize]; defaults to
     * [KetoyCacheConfig.DEFAULT] (network-first, 30-day max age, background
     * refresh enabled).
     */
    internal var cacheConfig: KetoyCacheConfig = KetoyCacheConfig.DEFAULT

    private val backgroundScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            Log.w(TAG, "Background fetch failed: ${e.message}")
        }
    )

    /** Tracks screens currently being background-fetched to prevent duplicates. */
    private val backgroundFetchInProgress = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // ── Public API ──────────────────────────────────────────────

    /**
     * Result of a screen fetch operation.
     *
     * Sealed hierarchy with two outcomes:
     * - [Success] — screen loaded (from cache or network).
     * - [Error] — fetch failed with a human-readable message.
     *
     * ```kotlin
     * when (val r = KetoyCloudService.fetchScreen("home")) {
     *     is FetchResult.Success -> Log.d("UI", "Got ${r.screenName} v${r.version}")
     *     is FetchResult.Error   -> Log.e("UI", r.message, r.cause)
     * }
     * ```
     *
     * @see fetchScreen
     */
    sealed class FetchResult {
        /**
         * Screen loaded successfully.
         *
         * @property screenName The resolved screen identifier.
         * @property version    Version string returned by the server or stored in cache.
         * @property uiBytes    The UI tree as raw bytes (wire format or plain JSON UTF-8),
         *                      ready to be passed to [JSONBytesToUI].
         * @property fromCache  `true` if the content came from the local cache rather
         *                      than a fresh network response.
         */
        data class Success(
            val screenName: String,
            val version: String,
            val uiBytes: ByteArray,
            val fromCache: Boolean
        ) : FetchResult()

        /**
         * Screen fetch failed.
         *
         * @property screenName The screen that was requested.
         * @property message    Human-readable error description suitable for logging.
         * @property cause      Optional underlying exception (e.g. [KetoyNetworkException]).
         */
        data class Error(
            val screenName: String,
            val message: String,
            val cause: Exception? = null
        ) : FetchResult()
    }

    /**
     * Fetch a screen using the configured cache strategy.
     *
     * This is the **main entry point** for all screen data retrieval.
     * It delegates to the appropriate strategy handler based on the current
     * [cacheConfig] value.
     *
     * The method runs entirely on [Dispatchers.IO][kotlinx.coroutines.Dispatchers.IO]
     * and never blocks the main thread.
     *
     * ```kotlin
     * val result = KetoyCloudService.fetchScreen("home_screen")
     * when (result) {
     *     is FetchResult.Success -> render(result.uiJson)
     *     is FetchResult.Error   -> showError(result.message)
     * }
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return [FetchResult.Success] with the screen JSON, or
     *         [FetchResult.Error] with an error message.
     * @see KetoyCacheStrategy
     * @see KetoyCloud.hasUpdate
     */
    suspend fun fetchScreen(screenName: String): FetchResult {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val cached = KetoyCacheStore.get(screenName)
                val isCacheValid = isCacheValid(cached)

                when (cacheConfig.strategy) {
                    KetoyCacheStrategy.NETWORK_FIRST ->
                        handleNetworkFirst(screenName, cached)

                    KetoyCacheStrategy.CACHE_FIRST ->
                        handleCacheFirst(screenName, cached, isCacheValid)

                    KetoyCacheStrategy.OPTIMISTIC ->
                        handleOptimistic(screenName, cached, isCacheValid)

                    KetoyCacheStrategy.CACHE_ONLY ->
                        handleCacheOnly(screenName, cached)

                    KetoyCacheStrategy.NETWORK_ONLY ->
                        handleNetworkOnly(screenName)
                }
            } catch (e: Exception) {
                FetchResult.Error(screenName, e.message ?: "Unknown error", e)
            }
        }
    }

    /**
     * Check if a screen has an updated version on the server.
     *
     * Compares the local cached version with the server version
     * using the lightweight `GET /api/v1/screen/version` endpoint.
     * Makes no full JSON download, so it is safe to call frequently
     * (e.g. on app resume).
     *
     * ```kotlin
     * val outdated = KetoyCloudService.hasUpdate("home_screen")
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return `true` if the server has a newer version or no local cache
     *         exists; `false` if versions match or the check fails.
     * @see KetoyApiClient.fetchScreenVersion
     */
    suspend fun hasUpdate(screenName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cachedVersion = KetoyCacheStore.getVersion(screenName) ?: return@withContext true
                val serverVersion = KetoyApiClient.fetchScreenVersion(screenName)
                serverVersion.version != cachedVersion
            } catch (e: Exception) {
                Log.w(TAG, "Version check failed for '$screenName': ${e.message}")
                false
            }
        }
    }

    /**
     * Clear the cache entry for a specific screen.
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return `true` if the screen was found and removed.
     * @see clearAllCache
     */
    fun clearScreenCache(screenName: String): Boolean {
        return KetoyCacheStore.remove(screenName)
    }

    /**
     * Clear all cached screens from [KetoyCacheStore].
     *
     * @see clearScreenCache
     */
    fun clearAllCache() {
        KetoyCacheStore.clearAll()
    }

    /**
     * Get the names of all screens currently present in the local cache.
     *
     * @return Immutable [Set] of cached screen identifiers.
     * @see KetoyCacheStore.getAllCachedScreenNames
     */
    fun getCachedScreenNames(): Set<String> {
        return KetoyCacheStore.getAllCachedScreenNames()
    }

    // ── Strategy Handlers ───────────────────────────────────────

    /**
     * **NETWORK_FIRST** strategy handler.
     *
     * Attempts a network fetch first. On failure, falls back to the
     * locally cached entry (regardless of its age). If no cache exists
     * and the network also fails, returns [FetchResult.Error].
     *
     * @param screenName Screen identifier.
     * @param cached     Pre-loaded cache entry (may be `null`).
     * @return [FetchResult] with screen data or an error.
     */
    private fun handleNetworkFirst(
        screenName: String,
        cached: KetoyCacheEntry?
    ): FetchResult {
        return try {
            val data = fetchFromNetwork(screenName, saveToCache = true)
            FetchResult.Success(
                screenName = data.screenName,
                version = data.version,
                uiBytes = data.ui.toString().toByteArray(Charsets.UTF_8),
                fromCache = false
            )
        } catch (e: Exception) {
            // Network failed – fall back to cache
            if (cached != null) {
                Log.d(TAG, "Network failed for '$screenName', using cache (v${cached.version})")
                FetchResult.Success(
                    screenName = cached.screenName,
                    version = cached.version,
                    uiBytes = cached.jsonContent.toByteArray(Charsets.UTF_8),
                    fromCache = true
                )
            } else {
                FetchResult.Error(
                    screenName = screenName,
                    message = "Network failed and no cached data available: ${e.message}",
                    cause = e
                )
            }
        }
    }

    /**
     * **CACHE_FIRST** strategy handler.
     *
     * Returns a valid (non-expired) cached entry immediately. If the
     * cache is stale or missing, falls back to a network fetch. Optionally
     * schedules a background refresh for the next load when
     * [KetoyCacheConfig.refreshInBackground] is enabled.
     *
     * @param screenName   Screen identifier.
     * @param cached        Pre-loaded cache entry (may be `null`).
     * @param isCacheValid `true` if [cached] is within [KetoyCacheConfig.maxAge].
     * @return [FetchResult] with screen data or an error.
     */
    private fun handleCacheFirst(
        screenName: String,
        cached: KetoyCacheEntry?,
        isCacheValid: Boolean
    ): FetchResult {
        // If cache is valid, use it
        if (cached != null && isCacheValid) {
            // Optionally refresh in background for next load
            if (cacheConfig.refreshInBackground) {
                fetchInBackground(screenName, cachedVersion = cached.version)
            }
            return FetchResult.Success(
                screenName = cached.screenName,
                version = cached.version,
                uiBytes = cached.jsonContent.toByteArray(Charsets.UTF_8),
                fromCache = true
            )
        }

        // Cache invalid or missing – fetch from network
        return try {
            val data = fetchFromNetwork(screenName, saveToCache = true)
            FetchResult.Success(
                screenName = data.screenName,
                version = data.version,
                uiBytes = data.ui.toString().toByteArray(Charsets.UTF_8),
                fromCache = false
            )
        } catch (e: Exception) {
            // Network also failed – use stale cache if available
            if (cached != null) {
                Log.d(TAG, "Network failed for '$screenName', using stale cache")
                FetchResult.Success(
                    screenName = cached.screenName,
                    version = cached.version,
                    uiBytes = cached.jsonContent.toByteArray(Charsets.UTF_8),
                    fromCache = true
                )
            } else {
                FetchResult.Error(screenName, "No cache and network failed: ${e.message}", e)
            }
        }
    }

    /**
     * **OPTIMISTIC** (stale-while-revalidate) strategy handler.
     *
     * Returns the cached entry immediately (even if expired) **and**
     * triggers a background fetch so the next load gets fresh data.
     * If no cache exists at all, falls back to a synchronous network call.
     *
     * @param screenName   Screen identifier.
     * @param cached        Pre-loaded cache entry (may be `null`).
     * @param isCacheValid  Unused for optimistic — always returns cache.
     * @return [FetchResult] with screen data or an error.
     */
    private fun handleOptimistic(
        screenName: String,
        cached: KetoyCacheEntry?,
        isCacheValid: Boolean
    ): FetchResult {
        if (cached != null) {
            // Return cache immediately + background refresh
            fetchInBackground(screenName, cachedVersion = cached.version)
            return FetchResult.Success(
                screenName = cached.screenName,
                version = cached.version,
                uiBytes = cached.jsonContent.toByteArray(Charsets.UTF_8),
                fromCache = true
            )
        }

        // No cache – must fetch from network
        return try {
            val data = fetchFromNetwork(screenName, saveToCache = true)
            FetchResult.Success(
                screenName = data.screenName,
                version = data.version,
                uiBytes = data.ui.toString().toByteArray(Charsets.UTF_8),
                fromCache = false
            )
        } catch (e: Exception) {
            FetchResult.Error(screenName, "No cache and network failed: ${e.message}", e)
        }
    }

    /**
     * **CACHE_ONLY** strategy handler.
     *
     * Returns cached data when available; never makes a network request.
     * Returns [FetchResult.Error] when no cache exists.
     *
     * @param screenName Screen identifier.
     * @param cached      Pre-loaded cache entry (may be `null`).
     * @return [FetchResult] with cached data or an error.
     */
    private fun handleCacheOnly(
        screenName: String,
        cached: KetoyCacheEntry?
    ): FetchResult {
        return if (cached != null) {
            FetchResult.Success(
                screenName = cached.screenName,
                version = cached.version,
                uiBytes = cached.jsonContent.toByteArray(Charsets.UTF_8),
                fromCache = true
            )
        } else {
            FetchResult.Error(
                screenName = screenName,
                message = "No cached data for '$screenName' (cache-only mode)"
            )
        }
    }

    /**
     * **NETWORK_ONLY** strategy handler.
     *
     * Always fetches from the network; never reads or writes the cache.
     * Returns [FetchResult.Error] when the network request fails.
     *
     * @param screenName Screen identifier.
     * @return [FetchResult] with fresh data or an error.
     */
    private fun handleNetworkOnly(screenName: String): FetchResult {
        return try {
            val data = fetchFromNetwork(screenName, saveToCache = false)
            FetchResult.Success(
                screenName = data.screenName,
                version = data.version,
                uiBytes = data.ui.toString().toByteArray(Charsets.UTF_8),
                fromCache = false
            )
        } catch (e: Exception) {
            FetchResult.Error(screenName, "Network request failed: ${e.message}", e)
        }
    }

    // ── Internal Helpers ────────────────────────────────────────

    /**
     * Fetch a screen from the Ketoy API and optionally save it to the local cache.
     *
     * @param screenName  Screen identifier passed to the API.
     * @param saveToCache If `true`, the response is persisted via [KetoyCacheStore.put].
     * @return [KetoyScreenData] containing the screen name, version, and JSON UI tree.
     * @throws KetoyNetworkException on HTTP or API errors.
     */
    private fun fetchFromNetwork(screenName: String, saveToCache: Boolean): KetoyScreenData {
        val data = KetoyApiClient.fetchScreenOptimized(screenName)

        if (saveToCache) {
            KetoyCacheStore.putBytes(
                screenName = data.screenName,
                version = data.version,
                data = data.ui.toString().toByteArray(Charsets.UTF_8)
            )
            Log.d(TAG, "Cached screen '${data.screenName}' v${data.version}")
        }

        return data
    }

    /**
     * Launch a background fetch to silently update the cache.
     *
     * First performs a lightweight version check via
     * [KetoyApiClient.fetchScreenVersion]. If the server reports the same
     * version as [cachedVersion], no further download occurs.
     *
     * A per-screen guard ensures only one background fetch runs at a time.
     *
     * @param screenName    Screen identifier.
     * @param cachedVersion Current cached version (may be `null`).
     */
    private fun fetchInBackground(screenName: String, cachedVersion: String?) {
        if (backgroundFetchInProgress.contains(screenName)) return

        backgroundFetchInProgress.add(screenName)

        backgroundScope.launch {
            try {
                // First check if version has changed (lightweight call)
                val serverVersion = KetoyApiClient.fetchScreenVersion(screenName)
                if (cachedVersion != null && serverVersion.version == cachedVersion) {
                    Log.d(TAG, "Background check: '$screenName' is up-to-date (v$cachedVersion)")
                    return@launch
                }

                // Version changed – fetch full screen
                val data = KetoyApiClient.fetchScreenOptimized(screenName)
                KetoyCacheStore.putBytes(
                    screenName = data.screenName,
                    version = data.version,
                    data = data.ui.toString().toByteArray(Charsets.UTF_8)
                )
                Log.d(TAG, "Background updated '$screenName' to v${data.version}")
            } catch (e: Exception) {
                Log.w(TAG, "Background fetch failed for '$screenName': ${e.message}")
            } finally {
                backgroundFetchInProgress.remove(screenName)
            }
        }
    }

    /**
     * Check whether a cache entry is still valid based on [KetoyCacheConfig.maxAge].
     *
     * @param entry The cache entry to validate (may be `null`).
     * @return `true` if the entry is non-null and its age is within [maxAge].
     *         Returns `true` for a non-null entry when [maxAge] is `null`
     *         (version-only expiration).
     */
    private fun isCacheValid(entry: KetoyCacheEntry?): Boolean {
        if (entry == null) return false
        val maxAge = cacheConfig.maxAge ?: return true // null maxAge = never expires by time
        val age = System.currentTimeMillis() - entry.cachedAt
        return age < maxAge.inWholeMilliseconds
    }
}
