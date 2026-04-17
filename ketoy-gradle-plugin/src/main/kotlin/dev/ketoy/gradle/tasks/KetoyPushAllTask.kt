package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.util.Base64

/**
 * Upload ALL exported screen `.ktw` files to the Ketoy Cloud server as a
 * single bundle.
 *
 * Endpoint: `POST /apps/{appId}/screens/bundle/ktw`
 * with a JSON body listing each screen as `{screenId, version, ktw(base64)}`.
 *
 * All versions are validated upfront by the server — the entire request
 * is rejected if any version is already taken.
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyPushAll -PscreenVersion=1.0.0
 * ```
 *
 * The `.ktw` files are read from the production export directory
 * (`ketoy-export/` by default) — run `ketoyExportProd` first to generate them.
 */
abstract class KetoyPushAllTask : DefaultTask() {

    @get:Internal
    lateinit var extension: KetoyDevExtension

    @TaskAction
    fun execute() {
        val token = extension.apiKey.orNull
            ?: throw GradleException(missingConfig("apiKey", "KETOY_DEVELOPER_API_KEY"))
        val appId = extension.appId.orNull
            ?: throw GradleException(missingConfig("appId", "KETOY_APP_ID"))
        val baseUrl = extension.baseUrl.get().trimEnd('/')
        val exportDir = extension.prodExportDir.getOrElse("ketoy-export")

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

        val exportDirFile = project.rootProject.file(exportDir)
        val ktwFiles = exportDirFile.listFiles()?.filter { it.extension == "ktw" } ?: emptyList()

        if (ktwFiles.isEmpty()) {
            throw GradleException(
                "No .ktw files found in $exportDir/. Run `./gradlew ketoyExportProd` first."
            )
        }

        // ── Build the bundle JSON body ──
        val encoder = Base64.getEncoder()
        val screensJson = ktwFiles.joinToString(",") { file ->
            val screenId = file.nameWithoutExtension
            val b64 = encoder.encodeToString(file.readBytes())
            """{"screenId":"$screenId","version":"$version","ktw":"$b64"}"""
        }
        val body = """{"bundleVersion":"$version","screens":[$screensJson]}"""

        val url = "$baseUrl/apps/$appId/screens/bundle/ktw"

        logger.lifecycle("")
        logger.lifecycle("╔════════════════════════════════════════════╗")
        logger.lifecycle("║     Ketoy Bundle Screen Upload (KTW)       ║")
        logger.lifecycle("╚════════════════════════════════════════════╝")
        logger.lifecycle("  Server:   $baseUrl")
        logger.lifecycle("  App ID:   $appId")
        logger.lifecycle("  Version:  $version")
        logger.lifecycle("  Screens:  ${ktwFiles.size} file(s)")
        ktwFiles.forEach { logger.lifecycle("    • ${it.nameWithoutExtension} (${it.length()} bytes)") }
        logger.lifecycle("")
        logger.lifecycle("  Uploading bundle...")

        val (code, response) = KetoyHttpClient.request("POST", url, token, body)

        if (code in 200..299) {
            logger.lifecycle("  ✔ Bundle upload successful (HTTP $code)")
            logger.lifecycle("  $response")
        } else {
            logger.lifecycle("  ✖ Bundle upload failed (HTTP $code)")
            logger.lifecycle("  $response")
            throw GradleException("Bundle upload failed with HTTP $code")
        }
        logger.lifecycle("")
    }
}
