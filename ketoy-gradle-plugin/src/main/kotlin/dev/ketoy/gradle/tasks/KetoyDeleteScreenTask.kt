package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Delete a screen and all its versions from the Ketoy Cloud server.
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
                | Usage:  ./gradlew ketoyDeleteScreen -PscreenName=home
                |
                """.trimMargin()
            )

        val url = "$baseUrl/api/screens/$packageName/$screenName"

        logger.lifecycle("")
        logger.lifecycle("  ⚠ Deleting '$screenName' (all versions) ...")
        logger.lifecycle("")

        val (code, response) = KetoyHttpClient.request("DELETE", url, apiKey)

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
