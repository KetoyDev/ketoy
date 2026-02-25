package com.developerstring.ketoy.cloud.cache

import kotlinx.serialization.Serializable

/**
 * Represents a single cached screen (or nav graph) entry stored locally.
 *
 * Contains the screen’s full JSON UI tree, a version identifier, and
 * a timestamp used for time-based staleness checks against
 * [KetoyCacheConfig.maxAge].
 *
 * Instances are serialised to JSON and persisted by [KetoyCacheStore]:
 * - **Metadata** (version, timestamp) goes into `SharedPreferences`.
 * - **JSON content** goes into a separate internal file.
 *
 * ## Example JSON representation (as stored in SharedPreferences)
 * ```json
 * {
 *   "screenName": "home_screen",
 *   "version": "1.0.0",
 *   "jsonContent": "",
 *   "cachedAt": 1739961600000
 * }
 * ```
 * > **Note:** `jsonContent` is stored as an empty string in SharedPreferences;
 * > the actual UI JSON is stored separately on disk to handle large payloads.
 *
 * @property screenName  The screen identifier (e.g. `"home_screen"`).
 *                       Also used as the file name in the cache directory.
 * @property version     The version string from the server (e.g. `"1.0.0"`).
 *                       Used for lightweight version comparison.
 * @property jsonContent The full JSON UI tree for the screen. When read back
 *                       from [KetoyCacheStore.get], this contains the actual
 *                       JSON content loaded from disk.
 * @property cachedAt    Epoch millis when this entry was cached.
 *                       Defaults to [System.currentTimeMillis] at construction.
 * @see KetoyCacheStore
 * @see KetoyCacheConfig
 */
@Serializable
data class KetoyCacheEntry(
    val screenName: String,
    val version: String,
    val jsonContent: String,
    val cachedAt: Long = System.currentTimeMillis()
)
