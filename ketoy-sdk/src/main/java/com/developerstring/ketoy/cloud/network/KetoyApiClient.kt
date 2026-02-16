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
 * Low-level HTTP client for the Ketoy SDUI API.
 *
 * Makes authenticated requests to fetch screens and check versions.
 * Uses [HttpURLConnection] to avoid external dependencies (OkHttp/Ktor).
 *
 * ## API Endpoints
 * - `GET /api/v1/screen?screen_name={name}` – Full screen JSON
 * - `GET /api/v1/screen/version?screen_name={name}` – Version check only
 *
 * ## Authentication
 * Every request includes:
 * - `x-api-key` header
 * - `x-package-name` header
 */
object KetoyApiClient {

    private var config: KetoyCloudConfig? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    /**
     * Initialize with cloud configuration.
     */
    fun initialize(config: KetoyCloudConfig) {
        this.config = config
    }

    /**
     * Fetch the full screen JSON from the server.
     *
     * @param screenName The screen identifier (e.g. "home_screen").
     * @return [KetoyScreenData] containing the screen JSON + version,
     *         or null if the request fails.
     * @throws KetoyNetworkException on network/API errors.
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
     * Mobile apps can call this first to decide whether to download
     * the full screen JSON.
     *
     * @param screenName The screen identifier.
     * @return [KetoyScreenVersionData] with version info, or null on failure.
     * @throws KetoyNetworkException on network/API errors.
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
 * @param message    Human-readable error description.
 * @param statusCode HTTP status code, if available.
 */
class KetoyNetworkException(
    message: String,
    val statusCode: Int? = null
) : Exception(message)
