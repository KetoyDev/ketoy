package dev.ketoy.gradle.internal

import java.net.HttpURLConnection
import java.net.URI

/**
 * Lightweight HTTP client using plain JDK — no extra dependencies.
 *
 * Every request sends the developer API key as an `x-developer-api-key`
 * header so that the Ketoy Cloud server can authenticate the caller.
 */
internal object KetoyHttpClient {

    /**
     * Perform an HTTP request and return the status code + response body.
     *
     * @param method  HTTP method (`GET`, `POST`, `DELETE`, etc.)
     * @param url     Full URL to call.
     * @param apiKey  Developer API key header value.
     * @param body    Optional JSON request body for `POST`/`PUT`.
     * @return A [Pair] of HTTP status code and the response body string.
     */
    fun request(
        method: String,
        url: String,
        apiKey: String,
        body: String? = null,
    ): Pair<Int, String> {
        val conn = URI(url).toURL().openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.setRequestProperty("x-developer-api-key", apiKey)
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000

        if (body != null) {
            conn.doOutput = true
            conn.outputStream.bufferedWriter().use { it.write(body) }
        }

        val code = conn.responseCode
        val responseBody = try {
            conn.inputStream.bufferedReader().readText()
        } catch (_: Exception) {
            conn.errorStream?.bufferedReader()?.readText() ?: ""
        }
        return code to responseBody
    }
}
