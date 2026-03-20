package dev.ketoy.gradle.server

/**
 * Immutable configuration for the Ketoy Dev Server.
 *
 * @property port The TCP port for the HTTP server. The WebSocket server uses `port + 1`. Default `8484`.
 * @property watchDir Path to the directory that contains the screen JSON files to serve. Default `./ketoy-screens`.
 * @property autoExport When `true`, a [SourceWatcher] monitors Kotlin sources and automatically
 *   runs `gradlew ketoyExport` to regenerate JSON on every change. Default `false`.
 * @property projectRoot Root directory of the Gradle project. Used to locate the Gradle wrapper. Default `.`.
 * @property sourceDirs List of source directories (relative to [projectRoot]) to watch when [autoExport] is enabled.
 *   Default `["app/src/main/java", "app/src/main/kotlin"]`.
 * @property debounceMs Milliseconds to wait after the last source change before triggering an export. Default `1500`.
 */
data class ServerConfig(
    val port: Int = 8484,
    val watchDir: String = "./ketoy-screens",
    val autoExport: Boolean = false,
    val projectRoot: String = ".",
    val sourceDirs: List<String> = listOf("app/src/main/java", "app/src/main/kotlin"),
    val debounceMs: Long = 1500,
    val androidSdkDir: String? = null
)
