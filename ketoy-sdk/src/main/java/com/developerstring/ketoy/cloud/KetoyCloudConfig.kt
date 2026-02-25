package com.developerstring.ketoy.cloud

/**
 * Configuration for connecting to the Ketoy Cloud (SDUI backend).
 *
 * Passed to [Ketoy.initialize][com.developerstring.ketoy.Ketoy.initialize] to enable
 * server-driven screen fetching. Every API call made by the SDK includes the
 * [apiKey] and [packageName] as HTTP headers for authentication and routing.
 *
 * ## Minimal usage
 * ```kotlin
 * val cloudConfig = KetoyCloudConfig(
 *     apiKey      = "fa044a28d695b2fa...",
 *     packageName = "com.developerstring.myapp"
 * )
 * Ketoy.initialize(cloudConfig = cloudConfig)
 * ```
 *
 * ## Custom base URL (self-hosted backend)
 * ```kotlin
 * val cloudConfig = KetoyCloudConfig(
 *     apiKey      = "my-key",
 *     packageName = "com.example.app",
 *     baseUrl     = "https://sdui.internal.mycompany.com"
 * )
 * ```
 *
 * ## API headers sent with every request
 * | Header            | Value                    |
 * |-------------------|--------------------------|
 * | `x-api-key`       | [apiKey]                 |
 * | `x-package-name`  | [packageName]            |
 *
 * @property apiKey       API key for authenticating with the Ketoy backend.
 *                        Obtain this from your Ketoy Cloud dashboard.
 * @property packageName  The Android application package name (e.g.
 *                        `"com.example.myapp"`). Sent with every request
 *                        so the backend can resolve the correct project.
 * @property baseUrl      Base URL for the Ketoy API.
 *                        Defaults to [DEFAULT_BASE_URL] (`"https://api.ketoy.dev"`).
 *                        Override this when using a self-hosted Ketoy backend.
 * @see KetoyCloudService
 * @see com.developerstring.ketoy.cloud.network.KetoyApiClient
 */
data class KetoyCloudConfig(
    val apiKey: String,
    val packageName: String,
    val baseUrl: String = DEFAULT_BASE_URL
) {
    companion object {
        /** Default Ketoy Cloud API base URL: `"https://api.ketoy.dev"`. */
        const val DEFAULT_BASE_URL = "https://api.ketoy.dev"
    }
}
