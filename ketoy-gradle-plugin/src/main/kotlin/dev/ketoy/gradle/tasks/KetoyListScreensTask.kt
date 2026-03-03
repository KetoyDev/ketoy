package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * List all screens deployed on the Ketoy Cloud server for this app.
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyListScreens
 * ```
 */
abstract class KetoyListScreensTask : DefaultTask() {

    @get:Internal
    lateinit var extension: KetoyDevExtension

    @TaskAction
    fun execute() {
        val apiKey = extension.apiKey.orNull
            ?: throw GradleException(missingConfig("apiKey", "KETOY_DEVELOPER_API_KEY"))
        val packageName = extension.packageName.orNull
            ?: throw GradleException(missingConfig("packageName", "KETOY_PACKAGE_NAME"))
        val baseUrl = extension.baseUrl.get().trimEnd('/')

        val url = "$baseUrl/api/screens/$packageName"

        logger.lifecycle("")
        logger.lifecycle("  Fetching screens for $packageName ...")
        logger.lifecycle("")

        val (code, response) = KetoyHttpClient.request("GET", url, apiKey)

        if (code in 200..299) {
            logger.lifecycle("  ✔ Screens (HTTP $code):")
            logger.lifecycle("")
            logger.lifecycle("  $response")
        } else {
            logger.lifecycle("  ✖ Failed (HTTP $code)")
            logger.lifecycle("  $response")
            throw GradleException("Failed to list screens (HTTP $code)")
        }
        logger.lifecycle("")
    }
}
