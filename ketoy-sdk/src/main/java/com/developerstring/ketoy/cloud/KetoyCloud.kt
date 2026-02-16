package com.developerstring.ketoy.cloud

import com.developerstring.ketoy.cloud.cache.KetoyCacheStore

/**
 * Public facade for Ketoy Cloud operations.
 *
 * Provides a clean API for managing cached screens and checking
 * for updates from the Ketoy server.
 *
 * ## Cache management
 * ```kotlin
 * // Clear a specific screen's cache
 * KetoyCloud.clearScreenCache("home_screen")
 *
 * // Clear all cached screens
 * KetoyCloud.clearAllCache()
 *
 * // Get all cached screen names
 * val cached = KetoyCloud.getCachedScreenNames()
 * ```
 *
 * ## Version checking
 * ```kotlin
 * // Check if server has a newer version
 * val needsUpdate = KetoyCloud.hasUpdate("home_screen")
 * ```
 */
object KetoyCloud {

    /**
     * Clear the cache for a specific screen.
     *
     * @param screenName The screen identifier.
     * @return true if the screen was found and removed.
     */
    fun clearScreenCache(screenName: String): Boolean {
        return KetoyCloudService.clearScreenCache(screenName)
    }

    /**
     * Clear all cached screens.
     */
    fun clearAllCache() {
        KetoyCloudService.clearAllCache()
    }

    /**
     * Get all cached screen names.
     */
    fun getCachedScreenNames(): Set<String> {
        return KetoyCloudService.getCachedScreenNames()
    }

    /**
     * Check if a screen has an updated version on the server.
     *
     * Uses the lightweight version endpoint to compare local
     * vs. server versions without downloading the full JSON.
     *
     * @param screenName The screen identifier.
     * @return true if an update is available.
     */
    suspend fun hasUpdate(screenName: String): Boolean {
        return KetoyCloudService.hasUpdate(screenName)
    }

    /**
     * Check if a screen is cached locally.
     */
    fun isCached(screenName: String): Boolean {
        return KetoyCacheStore.isCached(screenName)
    }

    /**
     * Get the cached version of a screen, or null if not cached.
     */
    fun getCachedVersion(screenName: String): String? {
        return KetoyCacheStore.getVersion(screenName)
    }
}
