package com.developerstring.ketoy.cloud

/**
 * Configuration for connecting to the Ketoy Cloud (SDUI backend).
 *
 * Passed to [Ketoy.initialize] to enable server-driven screen fetching.
 *
 * ## Usage
 * ```kotlin
 * val cloudConfig = KetoyCloudConfig(
 *     apiKey = "fa044a28d695b2fa...",
 *     packageName = "com.developerstring.myapp",
 *     baseUrl = "https://api.ketoy.dev"
 * )
 *
 * Ketoy.initialize(cloudConfig = cloudConfig)
 * ```
 *
 * @param apiKey       API key for authenticating with the Ketoy backend.
 * @param packageName  The Android application package name sent with every request.
 * @param baseUrl      Base URL for the Ketoy API.
 *                     Defaults to `https://api.ketoy.dev`.
 */
data class KetoyCloudConfig(
    val apiKey: String,
    val packageName: String,
    val baseUrl: String = DEFAULT_BASE_URL
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://api.ketoy.dev"
    }
}
