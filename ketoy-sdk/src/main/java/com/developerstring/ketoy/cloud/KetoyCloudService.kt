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
 * Implements all five cache strategies:
 * - **NETWORK_FIRST** – Try network, fall back to cache
 * - **CACHE_FIRST** – Use valid cache, fall back to network
 * - **OPTIMISTIC** – Return cache instantly, refresh in background
 * - **CACHE_ONLY** – Only use cache, never network
 * - **NETWORK_ONLY** – Only use network, never cache
 *
 * ## Usage (internal – called by KetoyCloudView composable)
 * ```kotlin
 * val result = KetoyCloudService.fetchScreen("home_screen")
 * ```
 */
object KetoyCloudService {

    private const val TAG = "KetoyCloud"

    internal var cacheConfig: KetoyCacheConfig = KetoyCacheConfig.DEFAULT

    private val backgroundScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            Log.w(TAG, "Background fetch failed: ${e.message}")
        }
    )

    /** Tracks screens currently being background-fetched to prevent duplicates. */
    private val backgroundFetchInProgress = mutableSetOf<String>()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // ── Public API ──────────────────────────────────────────────

    /**
     * Result of a screen fetch operation.
     */
    sealed class FetchResult {
        /** Screen loaded successfully. */
        data class Success(
            val screenName: String,
            val version: String,
            val uiJson: String,
            val fromCache: Boolean
        ) : FetchResult()

        /** Screen fetch failed. */
        data class Error(
            val screenName: String,
            val message: String,
            val cause: Exception? = null
        ) : FetchResult()
    }

    /**
     * Fetch a screen using the configured cache strategy.
     *
     * This is the main entry-point – it delegates to the appropriate
     * strategy handler based on [cacheConfig].
     *
     * @param screenName The screen identifier (e.g. "home_screen").
     * @return [FetchResult] with the screen JSON or an error.
     */
    suspend fun fetchScreen(screenName: String): FetchResult {
        return withContext(Dispatchers.IO) {
            try {
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
     * using the lightweight version endpoint.
     *
     * @param screenName The screen identifier.
     * @return `true` if server has a newer version, `false` otherwise.
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
     * Clear the cache for a specific screen.
     */
    fun clearScreenCache(screenName: String): Boolean {
        return KetoyCacheStore.remove(screenName)
    }

    /**
     * Clear all cached screens.
     */
    fun clearAllCache() {
        KetoyCacheStore.clearAll()
    }

    /**
     * Get all cached screen names.
     */
    fun getCachedScreenNames(): Set<String> {
        return KetoyCacheStore.getAllCachedScreenNames()
    }

    // ── Strategy Handlers ───────────────────────────────────────

    /**
     * NETWORK_FIRST: Try network, fall back to cache.
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
                uiJson = data.ui.toString(),
                fromCache = false
            )
        } catch (e: Exception) {
            // Network failed – fall back to cache
            if (cached != null) {
                Log.d(TAG, "Network failed for '$screenName', using cache (v${cached.version})")
                FetchResult.Success(
                    screenName = cached.screenName,
                    version = cached.version,
                    uiJson = cached.jsonContent,
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
     * CACHE_FIRST: Use valid cache, fall back to network.
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
                uiJson = cached.jsonContent,
                fromCache = true
            )
        }

        // Cache invalid or missing – fetch from network
        return try {
            val data = fetchFromNetwork(screenName, saveToCache = true)
            FetchResult.Success(
                screenName = data.screenName,
                version = data.version,
                uiJson = data.ui.toString(),
                fromCache = false
            )
        } catch (e: Exception) {
            // Network also failed – use stale cache if available
            if (cached != null) {
                Log.d(TAG, "Network failed for '$screenName', using stale cache")
                FetchResult.Success(
                    screenName = cached.screenName,
                    version = cached.version,
                    uiJson = cached.jsonContent,
                    fromCache = true
                )
            } else {
                FetchResult.Error(screenName, "No cache and network failed: ${e.message}", e)
            }
        }
    }

    /**
     * OPTIMISTIC (stale-while-revalidate): Return cache immediately,
     * fetch in background for next load.
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
                uiJson = cached.jsonContent,
                fromCache = true
            )
        }

        // No cache – must fetch from network
        return try {
            val data = fetchFromNetwork(screenName, saveToCache = true)
            FetchResult.Success(
                screenName = data.screenName,
                version = data.version,
                uiJson = data.ui.toString(),
                fromCache = false
            )
        } catch (e: Exception) {
            FetchResult.Error(screenName, "No cache and network failed: ${e.message}", e)
        }
    }

    /**
     * CACHE_ONLY: Only use cache, never network.
     */
    private fun handleCacheOnly(
        screenName: String,
        cached: KetoyCacheEntry?
    ): FetchResult {
        return if (cached != null) {
            FetchResult.Success(
                screenName = cached.screenName,
                version = cached.version,
                uiJson = cached.jsonContent,
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
     * NETWORK_ONLY: Always fetch from network, never cache.
     */
    private fun handleNetworkOnly(screenName: String): FetchResult {
        return try {
            val data = fetchFromNetwork(screenName, saveToCache = false)
            FetchResult.Success(
                screenName = data.screenName,
                version = data.version,
                uiJson = data.ui.toString(),
                fromCache = false
            )
        } catch (e: Exception) {
            FetchResult.Error(screenName, "Network request failed: ${e.message}", e)
        }
    }

    // ── Internal Helpers ────────────────────────────────────────

    /**
     * Fetch a screen from the network and optionally save to cache.
     */
    private fun fetchFromNetwork(screenName: String, saveToCache: Boolean): KetoyScreenData {
        val data = KetoyApiClient.fetchScreen(screenName)

        if (saveToCache) {
            KetoyCacheStore.put(
                screenName = data.screenName,
                version = data.version,
                jsonContent = data.ui.toString()
            )
            Log.d(TAG, "Cached screen '${data.screenName}' v${data.version}")
        }

        return data
    }

    /**
     * Launch a background fetch to update the cache silently.
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
                val data = KetoyApiClient.fetchScreen(screenName)
                KetoyCacheStore.put(
                    screenName = data.screenName,
                    version = data.version,
                    jsonContent = data.ui.toString()
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
     * Check whether a cache entry is still valid based on maxAge.
     */
    private fun isCacheValid(entry: KetoyCacheEntry?): Boolean {
        if (entry == null) return false
        val maxAge = cacheConfig.maxAge ?: return true // null maxAge = never expires by time
        val age = System.currentTimeMillis() - entry.cachedAt
        return age < maxAge.inWholeMilliseconds
    }
}
