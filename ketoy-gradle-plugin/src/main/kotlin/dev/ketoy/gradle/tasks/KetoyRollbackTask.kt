package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.internal.KetoyHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.URLEncoder

/**
 * Rollback a screen to a previously uploaded version.
 *
 * Endpoint: `POST /apps/{appId}/screens/{screenId}/rollback`
 * Body: `{"version":"<restore-from>","newVersion":"<new-semver>"}`
 *
 * The server restores the payload of `version` and publishes it under a
 * brand-new `newVersion`. Both must be supplied.
 *
 * Usage:
 * ```bash
 * ./gradlew ketoyRollback -PscreenName=home -PfromVersion=1.0.0 -PnewVersion=2.0.0
 * ```
 */
abstract class KetoyRollbackTask : DefaultTask() {

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
                | Usage:  ./gradlew ketoyRollback -PscreenName=home -PfromVersion=1.0.0 -PnewVersion=2.0.0
                |
                """.trimMargin()
            )

        // The version we want to restore from. Accept -PfromVersion, or fall
        // back to -PscreenVersion / -Pversion for backwards compatibility.
        val fromVersion = (project.findProperty("fromVersion") as? String)
            ?: (project.findProperty("screenVersion") as? String)
            ?: (project.findProperty("version") as? String)?.takeIf { it != "unspecified" }
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PfromVersion=<existing_version>
                |
                | Usage:  ./gradlew ketoyRollback -PscreenName=$screenName -PfromVersion=1.0.0 -PnewVersion=2.0.0
                |
                """.trimMargin()
            )

        val newVersion = project.findProperty("newVersion") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PnewVersion=<new_semver>
                |
                | The rollback API restores a past payload as a brand-new version.
                |
                | Usage:  ./gradlew ketoyRollback -PscreenName=$screenName -PfromVersion=$fromVersion -PnewVersion=2.0.0
                |
                """.trimMargin()
            )

        val encodedScreen = URLEncoder.encode(screenName, "UTF-8")
        val url = "$baseUrl/apps/$appId/screens/$encodedScreen/rollback"
        val body = """{"version":"$fromVersion","newVersion":"$newVersion"}"""

        logger.lifecycle("")
        logger.lifecycle("  Rolling back '$screenName' from $fromVersion → $newVersion ...")
        logger.lifecycle("")

        val (code, response) = KetoyHttpClient.request("POST", url, token, body)

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
