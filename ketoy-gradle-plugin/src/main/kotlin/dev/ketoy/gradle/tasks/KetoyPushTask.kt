package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Upload a single screen JSON to the Ketoy Cloud server.
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyPush -PscreenName=home -Pversion=1.0.0
 * ./gradlew ketoyPush -PscreenName=home -Pversion=1.0.0 -PdisplayName="Home Screen"
 * ./gradlew ketoyPush -PscreenName=home -Pversion=2.0.0 -Pdescription="Updated" -Ptags=home,landing
 * ```
 */
abstract class KetoyPushTask : DefaultTask() {

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

        // ── Required: screenName ──
        val screenName = project.findProperty("screenName") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenName=<name>
                |
                | Usage:  ./gradlew ketoyPush -PscreenName=home -Pversion=1.0.0
                |
                | Available screens in $screensDir/:
                |   ${availableScreens(screensDir)}
                |
                """.trimMargin()
            )

        // ── Locate the JSON file ──
        val jsonFile = project.rootProject.file("$screensDir/$screenName.json")
        if (!jsonFile.exists()) {
            throw GradleException(
                """
                |
                | ✖ Screen file not found: $screensDir/$screenName.json
                |
                | Available screens: ${availableScreens(screensDir)}
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
                |         ./gradlew ketoyPush -PscreenName=$screenName -Pversion=1.0.0
                |
                """.trimMargin()
            )

        // ── Optional parameters ──
        val displayName = (project.findProperty("displayName") as? String)
            ?: screenName.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
        val description = (project.findProperty("description") as? String) ?: ""
        val category = (project.findProperty("category") as? String) ?: ""
        val tags = (project.findProperty("tags") as? String)
            ?.split(",")
            ?.map { "\"${it.trim()}\"" }
            ?.joinToString(",")

        // ── Read and escape JSON content ──
        val rawJson = jsonFile.readText()
        val escapedJson = escapeJson(rawJson)

        // ── Build request body ──
        val metadataBlock = buildString {
            append("\"metadata\": {")
            val parts = mutableListOf<String>()
            if (category.isNotBlank()) parts.add("\"category\": \"$category\"")
            if (tags != null) parts.add("\"tags\": [$tags]")
            append(parts.joinToString(", "))
            append("}")
        }

        val requestBody = buildString {
            append("{")
            append("\"screenName\": \"$screenName\",")
            append("\"displayName\": \"$displayName\",")
            if (description.isNotBlank()) append("\"description\": \"$description\",")
            append("\"version\": \"$version\",")
            append("\"jsonContent\": \"$escapedJson\",")
            append(metadataBlock)
            append("}")
        }

        // ── Upload ──
        val url = "$baseUrl/api/screens/$packageName/upload"
        logger.lifecycle("")
        logger.lifecycle("╔════════════════════════════════════════════╗")
        logger.lifecycle("║          Ketoy Screen Upload               ║")
        logger.lifecycle("╚════════════════════════════════════════════╝")
        logger.lifecycle("  Server:    $baseUrl")
        logger.lifecycle("  Package:   $packageName")
        logger.lifecycle("  Screen:    $screenName")
        logger.lifecycle("  Version:   $version")
        logger.lifecycle("  File:      $screensDir/$screenName.json")
        logger.lifecycle("  File size: ${rawJson.length} bytes")
        logger.lifecycle("")
        logger.lifecycle("  Uploading...")

        val (code, response) = KetoyHttpClient.request("POST", url, apiKey, requestBody)

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

    private fun availableScreens(screensDir: String): String {
        return project.rootProject.file(screensDir).listFiles()
            ?.filter { it.extension == "json" }
            ?.joinToString(", ") { it.nameWithoutExtension }
            ?: "(none found)"
    }
}

/** Escape a raw JSON string so it can be embedded as a JSON string value. */
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
