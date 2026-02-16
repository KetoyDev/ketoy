package com.developerstring.ketoy.cloud.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Response wrapper from the Ketoy API.
 *
 * All API responses follow this structure:
 * ```json
 * {
 *   "success": true,
 *   "data": { ... }
 * }
 * ```
 */
@Serializable
data class KetoyApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

/**
 * The `data` payload for a full screen fetch.
 *
 * ```json
 * {
 *   "screenName": "home_screen",
 *   "version": "1.0.0",
 *   "ui": { ... }
 * }
 * ```
 */
@Serializable
data class KetoyScreenData(
    val screenName: String,
    val version: String,
    val ui: JsonElement
)

/**
 * The `data` payload for a version-only check.
 *
 * ```json
 * {
 *   "screenName": "home_screen",
 *   "version": "1.0.0",
 *   "updatedAt": "2026-02-10T08:55:00.000Z"
 * }
 * ```
 */
@Serializable
data class KetoyScreenVersionData(
    val screenName: String,
    val version: String,
    val updatedAt: String? = null
)
