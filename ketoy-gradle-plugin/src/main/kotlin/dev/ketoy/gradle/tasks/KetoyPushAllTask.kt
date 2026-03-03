package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Upload ALL screen JSONs from the screens directory to the Ketoy Cloud server.
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyPushAll -Pversion=1.0.0
 * ```
 */
abstract class KetoyPushAllTask : DefaultTask() {

    @get:Internal
    lateinit var extension: KetoyDevExtension

    @TaskAction
    fun execute() {
        val apiKey = extension.apiKey.orNull
            ?: throw GradleException(missingConfig("apiKey", "KETOY_DEVELOPER_API_KEY"))
        val packageName = extension.packageName.orNull
            ?: throw GradleException(missingConfig("packageName", "KETOY_PACKAGE_NAME"))
        val baseUrl = extension.baseUrl.get().trimEnd('/')
        val screensDir = extension.screensDir.get()

        val version = (project.findProperty("screenVersion") as? String)
            ?: (project.findProperty("version") as? String)?.takeIf { it != "unspecified" }
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenVersion=<semver>
                |
                | Usage:  ./gradlew ketoyPushAll -PscreenVersion=1.0.0
                |         ./gradlew ketoyPushAll -Pversion=1.0.0
                |
                """.trimMargin()
            )

        val screensDirFile = project.rootProject.file(screensDir)
        val jsonFiles = screensDirFile.listFiles()?.filter { it.extension == "json" } ?: emptyList()

        if (jsonFiles.isEmpty()) {
            throw GradleException("No JSON files found in $screensDir/")
        }

        logger.lifecycle("")
        logger.lifecycle("╔════════════════════════════════════════════╗")
        logger.lifecycle("║       Ketoy Batch Screen Upload            ║")
        logger.lifecycle("╚════════════════════════════════════════════╝")
        logger.lifecycle("  Server:   $baseUrl")
        logger.lifecycle("  Package:  $packageName")
        logger.lifecycle("  Version:  $version")
        logger.lifecycle("  Screens:  ${jsonFiles.size} file(s)")
        logger.lifecycle("")

        var success = 0
        var failed = 0

        jsonFiles.forEach { file ->
            val screenName = file.nameWithoutExtension
            val rawJson = file.readText()
            val escapedJson = escapeJson(rawJson)

            val displayName = screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() }

            val requestBody = buildString {
                append("{")
                append("\"screenName\": \"$screenName\",")
                append("\"displayName\": \"$displayName\",")
                append("\"version\": \"$version\",")
                append("\"jsonContent\": \"$escapedJson\"")
                append("}")
            }

            val url = "$baseUrl/api/screens/$packageName/upload"
            logger.lifecycle("  ↑ $screenName ... ")

            try {
                val (code, response) = KetoyHttpClient.request("POST", url, apiKey, requestBody)
                if (code in 200..299) {
                    logger.lifecycle("    ✔ (HTTP $code)")
                    success++
                } else {
                    logger.lifecycle("    ✖ (HTTP $code)")
                    logger.lifecycle("    $response")
                    failed++
                }
            } catch (e: Exception) {
                logger.lifecycle("    ✖ (${e.message})")
                failed++
            }
        }

        logger.lifecycle("")
        logger.lifecycle("  Done: $success uploaded, $failed failed")
        logger.lifecycle("")

        if (failed > 0) {
            throw GradleException("$failed screen(s) failed to upload")
        }
    }
}
