package com.developerstring.ketoy.cloud.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Generic response wrapper from the Ketoy API.
 *
 * All API responses share this structure:
 * ```json
 * {
 *   "success": true,
 *   "data": { ... },
 *   "error": null
 * }
 * ```
 *
 * When `success` is `false`, the [error] field contains a human-readable
 * message describing the failure.
 *
 * @param T The type of the [data] payload (e.g. [KetoyScreenData],
 *          [KetoyScreenVersionData]).
 * @property success Whether the API call succeeded.
 * @property data    The response payload; `null` when [success] is `false`.
 * @property error   Error message from the server; `null` on success.
 * @see KetoyApiClient
 */
@Serializable
data class KetoyApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

/**
 * The `data` payload returned by the full screen fetch endpoint.
 *
 * **Endpoint:** `GET /api/v1/screen?screen_name={name}`
 *
 * ```json
 * {
 *   "screenName": "home_screen",
 *   "version": "1.0.0",
 *   "ui": {
 *     "type": "Column",
 *     "children": [
 *       { "type": "Text", "text": "Hello World" }
 *     ]
 *   }
 * }
 * ```
 *
 * @property screenName The server-side screen identifier
 *                      (e.g. `"home_screen"`).
 * @property version    Semantic version string (e.g. `"1.0.0"`) used
 *                      for cache invalidation.
 * @property ui         The raw JSON element representing the Ketoy UI
 *                      tree. Can describe a screen layout **or** a
 *                      navigation graph depending on the screen type.
 * @see KetoyScreenVersionData
 * @see KetoyApiClient.fetchScreen
 */
@Serializable
data class KetoyScreenData(
    val screenName: String,
    val version: String,
    val ui: JsonElement
)

/**
 * The `data` payload returned by the version-only check endpoint.
 *
 * **Endpoint:** `GET /api/v1/screen/version?screen_name={name}`
 *
 * A lightweight alternative to [KetoyScreenData] — returns only
 * version metadata without the full UI tree, saving bandwidth.
 *
 * ```json
 * {
 *   "screenName": "home_screen",
 *   "version": "1.2.0",
 *   "updatedAt": "2026-02-10T08:55:00.000Z"
 * }
 * ```
 *
 * @property screenName The server-side screen identifier
 *                      (e.g. `"home_screen"`).
 * @property version    Semantic version string (e.g. `"1.2.0"`).
 * @property updatedAt  ISO-8601 timestamp of the last server update.
 *                      May be `null` if the server does not provide it.
 * @see KetoyScreenData
 * @see KetoyApiClient.fetchScreenVersion
 */
@Serializable
data class KetoyScreenVersionData(
    val screenName: String,
    val version: String,
    val updatedAt: String? = null
)
