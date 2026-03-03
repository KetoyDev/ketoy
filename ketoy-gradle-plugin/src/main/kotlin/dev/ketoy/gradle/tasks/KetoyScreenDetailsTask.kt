package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Get details of a specific screen including its JSON content.
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
        val apiKey = extension.apiKey.orNull
            ?: throw GradleException(missingConfig("apiKey", "KETOY_DEVELOPER_API_KEY"))
        val packageName = extension.packageName.orNull
            ?: throw GradleException(missingConfig("packageName", "KETOY_PACKAGE_NAME"))
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

        val url = "$baseUrl/api/screens/$packageName/$screenName/details?includeJson=true"

        logger.lifecycle("")
        logger.lifecycle("  Fetching details for '$screenName' ...")
        logger.lifecycle("")

        val (code, response) = KetoyHttpClient.request("GET", url, apiKey)

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
