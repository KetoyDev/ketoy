package com.developerstring.ketoy.cloud.cache

/**
 * Defines caching strategies for Ketoy server-driven screens and
 * navigation graphs.
 *
 * Inspired by industry-standard patterns (service workers, HTTP
 * `Cache-Control`, and SWR / stale-while-revalidate). Each strategy
 * controls when and how cached data is used versus fresh network data.
 *
 * ## Configuration
 * Set the strategy globally during SDK initialisation:
 * ```kotlin
 * Ketoy.initialize(
 *     cacheConfig = KetoyCacheConfig(
 *         strategy = KetoyCacheStrategy.NETWORK_FIRST
 *     )
 * )
 * ```
 *
 * ## Strategy comparison
 * | Strategy       | First load source | Fallback    | Use case                      |
 * |----------------|-------------------|-------------|-------------------------------|
 * | NETWORK_FIRST  | Network           | Cache       | Real-time, frequently updated |
 * | CACHE_FIRST    | Cache (if valid)  | Network     | Offline-first apps            |
 * | OPTIMISTIC     | Cache (instant)   | Background  | UI layouts, fast perceived UX |
 * | CACHE_ONLY     | Cache             | Error       | Airplane mode, pre-cached     |
 * | NETWORK_ONLY   | Network           | Error       | Sensitive, non-cacheable data |
 *
 * @see KetoyCacheConfig
 * @see com.developerstring.ketoy.cloud.KetoyCloudService
 * @see com.developerstring.ketoy.cloud.KetoyCloudNavService
 */
enum class KetoyCacheStrategy {

    /**
     * Always fetch from network first; fall back to cache on failure.
     *
     * **Default strategy.** Guarantees the latest content when online.
     *
     * - Always attempts a network fetch first.
     * - Falls back to any cached entry (even stale) on network error.
     * - Ensures the freshest data when connectivity is available.
     *
     * **Best for:** Real-time dashboards, frequently changing screens,
     * e-commerce product listings.
     *
     * @see KetoyCacheConfig
     */
    NETWORK_FIRST,

    /**
     * Use cached data if available and valid; fall back to network.
     *
     * - Returns cache immediately when it is within [KetoyCacheConfig.maxAge].
     * - Fetches from network only when the cache is stale or missing.
     * - Optionally refreshes in background when
     *   [KetoyCacheConfig.refreshInBackground] is enabled.
     *
     * **Best for:** Offline-first apps, content that rarely changes,
     * editorial/CMS-driven screens.
     *
     * @see KetoyCacheConfig.refreshInBackground
     */
    CACHE_FIRST,

    /**
     * Return cached data immediately while fetching updates in background.
     * (Stale-while-revalidate / SWR pattern.)
     *
     * - Returns cached data instantly (even if expired).
     * - Triggers a background fetch to update the cache.
     * - The next load will see the fresh data.
     * - Delivers the **fastest perceived loading time**.
     *
     * **Best for:** UI layouts, nav graphs, content screens where instant
     * rendering matters more than showing the absolute latest data.
     *
     * @see KetoyCacheConfig.refreshInBackground
     */
    OPTIMISTIC,

    /**
     * Only use cached data; never make network requests.
     *
     * - Never makes network requests.
     * - Instant loading from cache.
     * - Returns an error if no cached data exists.
     *
     * **Best for:** Airplane / offline-only mode, pre-cached screens
     * downloaded during onboarding, and development / testing scenarios.
     */
    CACHE_ONLY,

    /**
     * Always fetch from network; never use or update cache.
     *
     * - Always downloads fresh data from the server.
     * - Fails without an active network connection.
     * - Does **not** read from or write to the local cache.
     *
     * **Best for:** Sensitive data that must not be stored locally,
     * one-time verification screens, or debugging.
     */
    NETWORK_ONLY
}
