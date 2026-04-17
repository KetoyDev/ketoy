package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.URLEncoder

/**
 * Upload a single screen `.ktw` binary to the Ketoy Cloud server.
 *
 * Endpoint: `POST /apps/{appId}/screens/{screenId}/ktw?version={version}`
 * with `Content-Type: application/octet-stream`.
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyPush -PscreenName=home -PscreenVersion=1.0.0
 * ./gradlew ketoyPush -PscreenName=home -PscreenVersion=2.0.0
 * ```
 *
 * The `.ktw` file is read from the production export directory
 * (`ketoy-export/` by default) — run `ketoyExportProd` first to generate it.
 */
abstract class KetoyPushTask : DefaultTask() {

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

        // ── Required: screenName ──
        val screenName = project.findProperty("screenName") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenName=<name>
                |
                | Usage:  ./gradlew ketoyPush -PscreenName=home -PscreenVersion=1.0.0
                |
                | Available .ktw files in $exportDir/:
                |   ${availableKtw(exportDir)}
                |
                """.trimMargin()
            )

        // ── Locate the .ktw file ──
        val ktwFile = project.rootProject.file("$exportDir/$screenName.ktw")
        if (!ktwFile.exists()) {
            throw GradleException(
                """
                |
                | ✖ Wire-format file not found: $exportDir/$screenName.ktw
                |
                | Did you run `./gradlew ketoyExportProd` first?
                |
                | Available .ktw files: ${availableKtw(exportDir)}
                |
                """.trimMargin()
            )
        }

        // ── Required: version ──
        val version = (project.findProperty("screenVersion") as? String)
            ?: (project.findProperty("version") as? String)?.takeIf { it != "unspecified" }
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenVersion=<semver>
                |
                | Usage:  ./gradlew ketoyPush -PscreenName=$screenName -PscreenVersion=1.0.0
                |
                """.trimMargin()
            )

        // ── Read binary & upload ──
        val body = ktwFile.readBytes()
        val encodedScreen = URLEncoder.encode(screenName, "UTF-8")
        val encodedVersion = URLEncoder.encode(version, "UTF-8")
        val url = "$baseUrl/apps/$appId/screens/$encodedScreen/ktw?version=$encodedVersion"

        logger.lifecycle("")
        logger.lifecycle("╔════════════════════════════════════════════╗")
        logger.lifecycle("║          Ketoy Screen Upload (KTW)         ║")
        logger.lifecycle("╚════════════════════════════════════════════╝")
        logger.lifecycle("  Server:    $baseUrl")
        logger.lifecycle("  App ID:    $appId")
        logger.lifecycle("  Screen:    $screenName")
        logger.lifecycle("  Version:   $version")
        logger.lifecycle("  File:      $exportDir/$screenName.ktw")
        logger.lifecycle("  File size: ${body.size} bytes")
        logger.lifecycle("")
        logger.lifecycle("  Uploading...")

        val (code, response) = KetoyHttpClient.requestBinary("POST", url, token, body)

        if (code in 200..299) {
            logger.lifecycle("  ✔ Upload successful! (HTTP $code)")
            logger.lifecycle("  $response")
        } else {
            logger.lifecycle("  ✖ Upload failed (HTTP $code)")
            logger.lifecycle("  $response")
            throw GradleException("Screen upload failed with HTTP $code")
        }
        logger.lifecycle("")
    }

    private fun availableKtw(exportDir: String): String {
        return project.rootProject.file(exportDir).listFiles()
            ?.filter { it.extension == "ktw" }
            ?.joinToString(", ") { it.nameWithoutExtension }
            ?: "(none found)"
    }
}

/**
 * Escape a raw JSON string so it can be embedded as a JSON string value.
 * Retained for utility use by tasks that still build small JSON payloads
 * by hand (e.g. bundle manifest fields).
 */
internal fun escapeJson(raw: String): String = raw
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")
    .replace("\t", "\\t")

/** Standard error message for missing configuration. */
internal fun missingConfig(dslName: String, propName: String): String =
    """
    |
    | ✖ Ketoy Dev: '$dslName' is not configured.
    |
    | Set it in your build.gradle.kts:
    |   ketoyDev {
    |       $dslName.set("...")
    |   }
    |
    | Or in local.properties:
    |   $propName=...
    |
    """.trimMargin()
