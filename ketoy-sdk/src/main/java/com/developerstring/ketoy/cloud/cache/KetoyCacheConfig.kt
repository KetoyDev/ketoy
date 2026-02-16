package com.developerstring.ketoy.cloud.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Configuration for Ketoy screen caching behaviour.
 *
 * Controls how screens are cached, when they expire, and how
 * updates are handled. Configured globally during initialization.
 *
 * ## Basic usage
 * ```kotlin
 * Ketoy.initialize(
 *     cacheConfig = KetoyCacheConfig(
 *         strategy = KetoyCacheStrategy.NETWORK_FIRST,
 *         maxAge = 30.days,
 *     )
 * )
 * ```
 *
 * ## Strategy + background refresh
 * ```kotlin
 * KetoyCacheConfig(
 *     strategy = KetoyCacheStrategy.CACHE_FIRST,
 *     maxAge = 1.hours,
 *     refreshInBackground = true
 * )
 * ```
 *
 * @param strategy            The caching strategy to use. Defaults to [KetoyCacheStrategy.NETWORK_FIRST].
 * @param maxAge              Maximum age before cached data is considered stale.
 *                            `null` means no time-based expiration (version-only).
 * @param refreshInBackground Whether to fetch fresh data in the background
 *                            when showing cached content. Defaults to `true`.
 */
data class KetoyCacheConfig(
    val strategy: KetoyCacheStrategy = KetoyCacheStrategy.NETWORK_FIRST,
    val maxAge: Duration? = 30.days,
    val refreshInBackground: Boolean = true
) {
    companion object {
        /** Default configuration: network-first, 30-day max age, background refresh enabled. */
        val DEFAULT = KetoyCacheConfig()
    }
}
