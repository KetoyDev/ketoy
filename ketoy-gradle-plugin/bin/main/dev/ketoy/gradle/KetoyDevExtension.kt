package dev.ketoy.gradle

import org.gradle.api.provider.Property

/**
 * Configuration extension for the Ketoy Dev plugin.
 *
 * Users configure this in their `build.gradle.kts`:
 * ```kotlin
 * ketoyDev {
 *     apiKey.set("your-developer-api-key")
 *     packageName.set("com.example.myapp")
 *     baseUrl.set("https://api.ketoy.dev")        // optional
 *     screensDir.set("ketoy-screens")               // optional
 * }
 * ```
 *
 * All values can also be supplied via Gradle project properties
 * (`-P` flags) or `local.properties`. The resolution order is:
 *
 * 1. Extension DSL value (highest priority)
 * 2. Gradle project property (`-PketoyApiKey=...`)
 * 3. `local.properties` file in the root project
 */
abstract class KetoyDevExtension {

    /**
     * Developer API key for authenticating with the Ketoy Cloud server.
     *
     * Obtain this by registering at your Ketoy Cloud dashboard.
     * Can also be set via `-PketoyApiKey=...` or `KETOY_DEVELOPER_API_KEY`
     * in `local.properties`.
     */
    abstract val apiKey: Property<String>

    /**
     * Android application package name (e.g. `"com.example.myapp"`).
     *
     * Sent as an identifier with every API request.
     * Can also be set via `-PketoyPackageName=...` or `KETOY_PACKAGE_NAME`
     * in `local.properties`.
     */
    abstract val packageName: Property<String>

    /**
     * Base URL of the Ketoy Cloud API server.
     * Defaults to `"https://api.ketoy.dev"`.
     *
     * Override when using a self-hosted backend.
     */
    abstract val baseUrl: Property<String>

    /**
     * Directory (relative to the root project) that contains screen JSON files.
     * Defaults to `"ketoy-screens"`.
     */
    abstract val screensDir: Property<String>
}
