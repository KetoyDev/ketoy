package com.developerstring.ketoy.cloud.network

import com.developerstring.ketoy.cloud.KetoyCloudConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Low-level HTTP client for the Ketoy SDUI REST API.
 *
 * Makes authenticated requests to fetch screen JSON and check versions.
 * Uses [HttpURLConnection] (part of the Android SDK) to avoid external
 * dependencies such as OkHttp or Ktor, keeping the library footprint minimal.
 *
 * ## API endpoints
 * | Method | Path                                          | Purpose               |
 * |--------|-----------------------------------------------|-----------------------|
 * | GET    | `/api/v1/screen?screen_name={name}`           | Full screen JSON      |
 * | GET    | `/api/v1/screen/version?screen_name={name}`   | Lightweight version   |
 *
 * ## Authentication headers
 * Every request includes:
 * - `x-api-key` — the API key from [KetoyCloudConfig.apiKey].
 * - `x-package-name` — the app package name from [KetoyCloudConfig.packageName].
 *
 * ## Initialisation
 * ```kotlin
 * // Called internally by Ketoy.initialize()
 * KetoyApiClient.initialize(
 *     KetoyCloudConfig(
 *         apiKey = "fa044a28d695b2fa...",
 *         packageName = "com.example.app"
 *     )
 * )
 * ```
 *
 * ## Timeouts
 * - Connect timeout: **15 seconds**
 * - Read timeout: **30 seconds**
 *
 * @see KetoyCloudConfig
 * @see KetoyScreenData
 * @see KetoyScreenVersionData
 * @see KetoyNetworkException
 */
object KetoyApiClient {

    /** Cloud configuration (API key, package name, base URL). Set via [initialize]. */
    private var config: KetoyCloudConfig? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    /**
     * Initialise the client with a [KetoyCloudConfig].
     *
     * Must be called before [fetchScreen] or [fetchScreenVersion].
     * This is handled automatically by
     * [Ketoy.initialize][com.developerstring.ketoy.Ketoy.initialize].
     *
     * ```kotlin
     * KetoyApiClient.initialize(cloudConfig)
     * ```
     *
     * @param config The cloud configuration containing the API key, package
     *               name, and base URL.
     */
    fun initialize(config: KetoyCloudConfig) {
        this.config = config
    }

    /**
     * Fetch the full screen JSON from the server.
     *
     * Calls `GET {baseUrl}/api/v1/screen?screen_name={screenName}` with
     * authentication headers and parses the JSON response.
     *
     * ## Expected API response
     * ```json
     * {
     *   "success": true,
     *   "data": {
     *     "screenName": "home_screen",
     *     "version": "1.0.0",
     *     "ui": { "type": "Column", "children": [...] }
     *   }
     * }
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return [KetoyScreenData] containing the screen name, version, and
     *         the `ui` [JsonElement] tree.
     * @throws KetoyNetworkException If the client is not initialised,
     *         the HTTP request fails, or the API returns `success: false`.
     * @see fetchScreenVersion
     */
    fun fetchScreen(screenName: String): KetoyScreenData {
        val cfg = config ?: throw KetoyNetworkException("KetoyApiClient not initialized. Call Ketoy.initialize() with cloudConfig first.")

        val url = "${cfg.baseUrl}/api/v1/screen?screen_name=$screenName"
        val responseBody = executeGet(url, cfg)

        val responseJson = json.parseToJsonElement(responseBody).jsonObject
        val success = responseJson["success"]?.jsonPrimitive?.content?.toBoolean() ?: false

        if (!success) {
            val error = responseJson["error"]?.jsonPrimitive?.content ?: "Unknown API error"
            throw KetoyNetworkException("API error for screen '$screenName': $error")
        }

        val data = responseJson["data"]?.jsonObject
            ?: throw KetoyNetworkException("Missing 'data' field in API response for screen '$screenName'")

        return KetoyScreenData(
            screenName = data["screenName"]?.jsonPrimitive?.content ?: screenName,
            version = data["version"]?.jsonPrimitive?.content ?: "0.0.0",
            ui = data["ui"] ?: throw KetoyNetworkException("Missing 'ui' field in screen data for '$screenName'")
        )
    }

    /**
     * Check the version of a screen on the server (lightweight call).
     *
     * Calls `GET {baseUrl}/api/v1/screen/version?screen_name={screenName}`.
     * Use this before [fetchScreen] to decide whether a full download is
     * needed, saving bandwidth on mobile networks.
     *
     * ## Expected API response
     * ```json
     * {
     *   "success": true,
     *   "data": {
     *     "screenName": "home_screen",
     *     "version": "1.2.0",
     *     "updatedAt": "2026-02-10T08:55:00.000Z"
     *   }
     * }
     * ```
     *
     * @param screenName The screen identifier (e.g. `"home_screen"`).
     * @return [KetoyScreenVersionData] with version info.
     * @throws KetoyNetworkException If the client is not initialised,
     *         the HTTP request fails, or the API returns `success: false`.
     * @see fetchScreen
     */
    fun fetchScreenVersion(screenName: String): KetoyScreenVersionData {
        val cfg = config ?: throw KetoyNetworkException("KetoyApiClient not initialized.")

        val url = "${cfg.baseUrl}/api/v1/screen/version?screen_name=$screenName"
        val responseBody = executeGet(url, cfg)

        val responseJson = json.parseToJsonElement(responseBody).jsonObject
        val success = responseJson["success"]?.jsonPrimitive?.content?.toBoolean() ?: false

        if (!success) {
            val error = responseJson["error"]?.jsonPrimitive?.content ?: "Unknown API error"
            throw KetoyNetworkException("API error checking version for '$screenName': $error")
        }

        val data = responseJson["data"]?.jsonObject
            ?: throw KetoyNetworkException("Missing 'data' field in version response for '$screenName'")

        return KetoyScreenVersionData(
            screenName = data["screenName"]?.jsonPrimitive?.content ?: screenName,
            version = data["version"]?.jsonPrimitive?.content ?: "0.0.0",
            updatedAt = data["updatedAt"]?.jsonPrimitive?.content
        )
    }

    // ── Internal HTTP ───────────────────────────────────────────

    /**
     * Execute an authenticated HTTP GET request and return the raw
     * response body.
     *
     * @param urlString Fully qualified URL.
     * @param cfg       Cloud configuration providing auth headers.
     * @return Raw response body as a [String].
     * @throws KetoyNetworkException on non-2xx HTTP status codes.
     */
    private fun executeGet(urlString: String, cfg: KetoyCloudConfig): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("x-api-key", cfg.apiKey)
            connection.setRequestProperty("x-package-name", cfg.packageName)
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                val errorBody = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (e: Exception) {
                    ""
                }
                throw KetoyNetworkException(
                    "HTTP $responseCode for $urlString: $errorBody",
                    responseCode
                )
            }

            return BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                reader.readText()
            }
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Exception thrown by the Ketoy network layer.
 *
 * Wraps HTTP and API errors with a human-readable message. When the
 * error originates from an HTTP response, [statusCode] contains the
 * HTTP status code (e.g. `401`, `404`, `500`).
 *
 * ```kotlin
 * try {
 *     val data = KetoyApiClient.fetchScreen("home")
 * } catch (e: KetoyNetworkException) {
 *     Log.e("Network", "Failed: ${e.message}, HTTP ${e.statusCode}")
 * }
 * ```
 *
 * @property message    Human-readable error description.
 * @property statusCode HTTP status code, if available; `null` for non-HTTP
 *                      errors (e.g. client not initialised).
 * @see KetoyApiClient
 */
class KetoyNetworkException(
    message: String,
    val statusCode: Int? = null
) : Exception(message)
