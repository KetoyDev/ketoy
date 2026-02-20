package com.developerstring.ketoy.cloud.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Configuration for Ketoy screen caching behaviour.
 *
 * Controls **how** screens are cached, **when** they expire, and whether
 * the SDK should silently refresh stale data in the background.
 * Configured globally during SDK initialisation and shared between
 * [KetoyCloudService][com.developerstring.ketoy.cloud.KetoyCloudService]
 * and [KetoyCloudNavService][com.developerstring.ketoy.cloud.KetoyCloudNavService].
 *
 * ## Basic usage
 * ```kotlin
 * Ketoy.initialize(
 *     cacheConfig = KetoyCacheConfig(
 *         strategy = KetoyCacheStrategy.NETWORK_FIRST,
 *         maxAge   = 30.days,
 *     )
 * )
 * ```
 *
 * ## Strategy + background refresh
 * ```kotlin
 * KetoyCacheConfig(
 *     strategy            = KetoyCacheStrategy.CACHE_FIRST,
 *     maxAge              = 1.hours,
 *     refreshInBackground = true
 * )
 * ```
 *
 * ## Disable time-based expiration (version-only)
 * ```kotlin
 * KetoyCacheConfig(
 *     strategy = KetoyCacheStrategy.CACHE_FIRST,
 *     maxAge   = null   // cache never expires by time; only by version mismatch
 * )
 * ```
 *
 * @property strategy            The caching strategy to use.
 *                               Defaults to [KetoyCacheStrategy.NETWORK_FIRST].
 * @property maxAge              Maximum age before cached data is considered stale.
 *                               `null` means no time-based expiration (version-only
 *                               invalidation). Defaults to `30.days`.
 * @property refreshInBackground Whether to fetch fresh data silently in the background
 *                               when returning cached content. Only applies to the
 *                               `CACHE_FIRST` and `OPTIMISTIC` strategies.
 *                               Defaults to `true`.
 * @see KetoyCacheStrategy
 * @see KetoyCacheStore
 * @see com.developerstring.ketoy.cloud.KetoyCloudService
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
