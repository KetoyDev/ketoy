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
 *     prodExportDir.set("ketoy-export")              // optional
 *     exportTestClass.set("KetoyAutoExportTest")    // optional
 *     appModule.set("app")                            // optional
 *     testTaskName.set("testDebugUnitTest")           // optional
 *     serverModule.set("ketoy-devtools-server")       // optional
 *     serverPort.set(8484)                              // optional
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
     * Ketoy Cloud application UUID returned by `POST /apps` when the app
     * is first created. Required by every authenticated cloud task
     * (`ketoyPush`, `ketoyListScreens`, `ketoyRollback`, ...).
     *
     * Can also be set via `-PketoyAppId=...` or `KETOY_APP_ID` in
     * `local.properties`.
     */
    abstract val appId: Property<String>

    /**
     * Android application package name / bundleId (e.g. `"com.example.myapp"`).
     *
     * Used as a human-readable label and referenced by the public mobile
     * SDK endpoint (`GET /ktw?app=...`). The authenticated cloud API
     * now keys off [appId] (a UUID), so `packageName` is no longer sent
     * with every request.
     *
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

    // ── Export & Dev Server ──────────────────────────────────────

    /**
     * Directory (relative to the root project) for production export output.
     * Defaults to `"ketoy-export"`.
     */
    abstract val prodExportDir: Property<String>

    /**
     * Simple name of the auto-export test class.
     * Defaults to `"KetoyAutoExportTest"`.
     *
     * The plugin configures the unit test task to run only this class when
     * `ketoyExport` or `ketoyExportProd` is invoked. The test class has
     * two methods: `exportForDevServer` and `exportForProduction`.
     */
    abstract val exportTestClass: Property<String>

    /**
     * @deprecated Use [exportTestClass] instead. Both dev and prod exports
     * now use the same auto-export test class (`KetoyAutoExportTest`).
     */
    @Deprecated("Use exportTestClass instead", ReplaceWith("exportTestClass"))
    abstract val prodExportTestClass: Property<String>

    /**
     * Gradle project path of the Android app module that contains the
     * export test classes. Defaults to `"app"`.
     *
     * Used to resolve the test task for `ketoyExport` / `ketoyExportProd`.
     */
    abstract val appModule: Property<String>

    /**
     * Name of the Android unit test task to execute for screen export.
     * Defaults to `"testDebugUnitTest"`.
     */
    abstract val testTaskName: Property<String>

    /**
     * HTTP port for the embedded Ketoy Dev Server.
     * Defaults to `8484`. The WebSocket server uses `port + 1`.
     *
     * Used by `ketoyServe` and `ketoyDev` tasks.
     */
    abstract val serverPort: Property<Int>
}
