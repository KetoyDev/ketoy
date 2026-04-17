package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.URLEncoder

/**
 * Delete a screen and all its versions from the Ketoy Cloud server.
 *
 * Endpoint: `DELETE /apps/{appId}/screens/{screenId}`
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyDeleteScreen -PscreenName=home
 * ```
 */
abstract class KetoyDeleteScreenTask : DefaultTask() {

    @get:Internal
    lateinit var extension: KetoyDevExtension

    @TaskAction
    fun execute() {
        val token = extension.apiKey.orNull
            ?: throw GradleException(missingConfig("apiKey", "KETOY_DEVELOPER_API_KEY"))
        val appId = extension.appId.orNull
            ?: throw GradleException(missingConfig("appId", "KETOY_APP_ID"))
        val baseUrl = extension.baseUrl.get().trimEnd('/')

        val screenName = project.findProperty("screenName") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenName=<name>
                |
                | Usage:  ./gradlew ketoyDeleteScreen -PscreenName=home
                |
                """.trimMargin()
            )

        val encodedScreen = URLEncoder.encode(screenName, "UTF-8")
        val url = "$baseUrl/apps/$appId/screens/$encodedScreen"

        logger.lifecycle("")
        logger.lifecycle("  ⚠ Deleting '$screenName' (all versions) ...")
        logger.lifecycle("")

        val (code, response) = KetoyHttpClient.request("DELETE", url, token)

        if (code in 200..299) {
            logger.lifecycle("  ✔ Deleted (HTTP $code)")
            logger.lifecycle("")
            logger.lifecycle("  $response")
        } else {
            logger.lifecycle("  ✖ Delete failed (HTTP $code)")
            logger.lifecycle("  $response")
            throw GradleException("Delete failed (HTTP $code)")
        }
        logger.lifecycle("")
    }
}
