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
 * Endpoint: `GET /apps/{appId}/screens`
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
        val token = extension.apiKey.orNull
            ?: throw GradleException(missingConfig("apiKey", "KETOY_DEVELOPER_API_KEY"))
        val appId = extension.appId.orNull
            ?: throw GradleException(missingConfig("appId", "KETOY_APP_ID"))
        val baseUrl = extension.baseUrl.get().trimEnd('/')

        val url = "$baseUrl/apps/$appId/screens"

        logger.lifecycle("")
        logger.lifecycle("  Fetching screens for app $appId ...")
        logger.lifecycle("")

        val (code, response) = KetoyHttpClient.request("GET", url, token)

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
