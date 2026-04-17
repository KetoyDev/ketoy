package dev.ketoy.gradle.internal

import java.net.HttpURLConnection
import java.net.URI

/**
 * Lightweight HTTP client using plain JDK — no extra dependencies.
 *
 * Every request sends the developer API key in the `X-Api-Key` header
 * so the Ketoy Cloud server can authenticate the caller.
 */
internal object KetoyHttpClient {

    /**
     * Perform an HTTP request with a JSON (or empty) body.
     *
     * @param method  HTTP method (`GET`, `POST`, `PUT`, `DELETE`, etc.)
     * @param url     Full URL to call.
     * @param token   Developer API key sent as `X-Api-Key: <token>`.
     * @param body    Optional JSON request body.
     * @return A [Pair] of HTTP status code and response body string.
     */
    fun request(
        method: String,
        url: String,
        token: String,
        body: String? = null,
    ): Pair<Int, String> {
        val conn = URI(url).toURL().openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.setRequestProperty("X-Api-Key", token)
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 15_000
        conn.readTimeout = 60_000

        if (body != null) {
            conn.doOutput = true
            conn.outputStream.bufferedWriter().use { it.write(body) }
        }

        return readResponse(conn)
    }

    /**
     * Perform an HTTP request with a raw binary body (e.g. a `.ktw` file upload).
     *
     * @param method      HTTP method, typically `POST`.
     * @param url         Full URL to call.
     * @param token       Developer API key sent as `X-Api-Key: <token>`.
     * @param body        Raw bytes to send as the request body.
     * @param contentType MIME type for the body. Defaults to `application/octet-stream`.
     */
    fun requestBinary(
        method: String,
        url: String,
        token: String,
        body: ByteArray,
        contentType: String = "application/octet-stream",
    ): Pair<Int, String> {
        val conn = URI(url).toURL().openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.setRequestProperty("X-Api-Key", token)
        conn.setRequestProperty("Content-Type", contentType)
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 15_000
        conn.readTimeout = 60_000

        conn.doOutput = true
        conn.setFixedLengthStreamingMode(body.size)
        conn.outputStream.use { it.write(body) }

        return readResponse(conn)
    }

    private fun readResponse(conn: HttpURLConnection): Pair<Int, String> {
        val code = conn.responseCode
        val responseBody = try {
            conn.inputStream.bufferedReader().readText()
        } catch (_: Exception) {
            conn.errorStream?.bufferedReader()?.readText() ?: ""
        }
        return code to responseBody
    }
}
