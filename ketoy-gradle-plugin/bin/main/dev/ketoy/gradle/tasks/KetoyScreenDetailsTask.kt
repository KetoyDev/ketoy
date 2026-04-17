package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.URLEncoder

/**
 * Get metadata for a specific screen.
 *
 * Endpoint: `GET /apps/{appId}/screens/{screenId}`
 *
 * Note: the new KTW API returns screen metadata only. To fetch the
 * binary payload of a specific version, use
 * `GET /apps/{appId}/screens/{screenId}/versions/{version}`.
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyScreenDetails -PscreenName=home
 * ```
 */
abstract class KetoyScreenDetailsTask : DefaultTask() {

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
                | Usage:  ./gradlew ketoyScreenDetails -PscreenName=home
                |
                """.trimMargin()
            )

        val encodedScreen = URLEncoder.encode(screenName, "UTF-8")
        val url = "$baseUrl/apps/$appId/screens/$encodedScreen"

        logger.lifecycle("")
        logger.lifecycle("  Fetching details for '$screenName' ...")
        logger.lifecycle("")

        val (code, response) = KetoyHttpClient.request("GET", url, token)

        if (code in 200..299) {
            logger.lifecycle("  ✔ Details (HTTP $code):")
            logger.lifecycle("")
            logger.lifecycle("  $response")
        } else {
            logger.lifecycle("  ✖ Failed (HTTP $code)")
            logger.lifecycle("  $response")
            throw GradleException("Failed to fetch screen details (HTTP $code)")
        }
        logger.lifecycle("")
    }
}
