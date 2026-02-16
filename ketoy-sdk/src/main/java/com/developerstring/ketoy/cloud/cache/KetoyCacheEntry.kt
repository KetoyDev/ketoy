package com.developerstring.ketoy.cloud.cache

import kotlinx.serialization.Serializable

/**
 * Represents a cached screen entry stored locally.
 *
 * Contains the screen's JSON content, version info, and
 * timestamp for staleness checks.
 *
 * @param screenName  The screen identifier (e.g. "home_screen").
 * @param version     The version string from the server (e.g. "1.0.0").
 * @param jsonContent The full JSON UI tree for the screen.
 * @param cachedAt    Epoch millis when this entry was cached.
 */
@Serializable
data class KetoyCacheEntry(
    val screenName: String,
    val version: String,
    val jsonContent: String,
    val cachedAt: Long = System.currentTimeMillis()
)
