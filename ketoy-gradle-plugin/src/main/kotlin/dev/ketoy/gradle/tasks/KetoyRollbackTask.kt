package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Rollback a screen to a previously uploaded version.
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyRollback -PscreenName=home -Pversion=1.0.0
 * ```
 */
abstract class KetoyRollbackTask : DefaultTask() {

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
                | Usage:  ./gradlew ketoyRollback -PscreenName=home -Pversion=1.0.0
                |
                """.trimMargin()
            )

        val version = (project.findProperty("screenVersion") as? String)
            ?: (project.findProperty("version") as? String)?.takeIf { it != "unspecified" }
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenVersion=<target_version>
                |
                | Usage:  ./gradlew ketoyRollback -PscreenName=$screenName -PscreenVersion=1.0.0
                |         ./gradlew ketoyRollback -PscreenName=$screenName -Pversion=1.0.0
                |
                """.trimMargin()
            )

        val url = "$baseUrl/api/screens/$packageName/$screenName/rollback/$version"

        logger.lifecycle("")
        logger.lifecycle("  Rolling back '$screenName' to version $version ...")
        logger.lifecycle("")

        val (code, response) = KetoyHttpClient.request("POST", url, apiKey)

        if (code in 200..299) {
            logger.lifecycle("  ✔ Rollback successful (HTTP $code)")
            logger.lifecycle("")
            logger.lifecycle("  $response")
        } else {
            logger.lifecycle("  ✖ Rollback failed (HTTP $code)")
            logger.lifecycle("  $response")
            throw GradleException("Rollback failed (HTTP $code)")
        }
        logger.lifecycle("")
    }
}
