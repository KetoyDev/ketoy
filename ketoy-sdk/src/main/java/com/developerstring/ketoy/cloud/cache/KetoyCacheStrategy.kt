package com.developerstring.ketoy.cloud.cache

/**
 * Defines caching strategies for Ketoy server-driven screens.
 *
 * Inspired by industry-standard patterns (service workers, HTTP caching,
 * and SWR). Each strategy controls when and how cached screen data is
 * used vs. fresh network data.
 *
 * Configure globally during initialization:
 * ```kotlin
 * Ketoy.initialize(
 *     cacheConfig = KetoyCacheConfig(
 *         strategy = KetoyCacheStrategy.NETWORK_FIRST
 *     )
 * )
 * ```
 */
enum class KetoyCacheStrategy {

    /**
     * Always fetch from network first; fall back to cache on failure.
     *
     * **Default strategy.**
     *
     * - Always fetches fresh data first
     * - Falls back to cache on network error
     * - Ensures latest content when online
     *
     * Best for: Real-time data, frequently changing screens.
     */
    NETWORK_FIRST,

    /**
     * Use cached data if available and valid; fall back to network.
     *
     * - Uses cache if valid (not expired)
     * - Fetches from network only when cache is stale/missing
     * - Optionally refreshes in background
     *
     * Best for: Offline-first apps, content that doesn't change often.
     */
    CACHE_FIRST,

    /**
     * Return cached data immediately while fetching updates in background.
     * (Stale-while-revalidate pattern)
     *
     * - Returns cached data instantly
     * - Fetches fresh data in background
     * - Updates cache for next load
     * - Fastest perceived loading
     *
     * Best for: UI layouts, content screens where instant loading
     * matters more than showing the absolute latest data.
     */
    OPTIMISTIC,

    /**
     * Only use cached data; never make network requests.
     *
     * - Never makes network requests
     * - Instant loading from cache
     * - Fails if no cached data exists
     *
     * Best for: Offline-only mode, airplane mode, pre-cached screens.
     */
    CACHE_ONLY,

    /**
     * Always fetch from network; never use or update cache.
     *
     * - Always fresh data
     * - Fails without network
     * - No offline support
     *
     * Best for: Sensitive data that shouldn't be cached.
     */
    NETWORK_ONLY
}
