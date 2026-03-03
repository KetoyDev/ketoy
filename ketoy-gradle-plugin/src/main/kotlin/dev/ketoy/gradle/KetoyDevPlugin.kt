package dev.ketoy.gradle

import dev.ketoy.gradle.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Properties

/**
 * Ketoy Dev — Gradle plugin for managing Ketoy SDUI screens.
 *
 * Registers the `ketoyDev {}` extension and the following tasks:
 *
 * | Task                    | Description                                        |
 * |-------------------------|----------------------------------------------------|
 * | `ketoyPush`             | Upload a single screen JSON to the cloud server    |
 * | `ketoyPushAll`          | Upload all screen JSONs from the screens directory |
 * | `ketoyListScreens`      | List all deployed screens for this app             |
 * | `ketoyScreenVersions`   | List all versions of a specific screen             |
 * | `ketoyScreenDetails`    | Get details of a screen including its JSON content |
 * | `ketoyRollback`         | Roll back a screen to a previous version           |
 * | `ketoyDeleteScreen`     | Delete a screen and all its versions               |
 *
 * ## Usage
 * ```kotlin
 * plugins {
 *     id("dev.ketoy.ketoy-dev") version "0.1.0"
 * }
 *
 * ketoyDev {
 *     apiKey.set("your-developer-api-key")
 *     packageName.set("com.example.myapp")
 * }
 * ```
 *
 * Then run tasks:
 * ```bash
 * ./gradlew ketoyPush -PscreenName=home -Pversion=1.0.0
 * ./gradlew ketoyPushAll -Pversion=1.0.0
 * ./gradlew ketoyListScreens
 * ./gradlew ketoyRollback -PscreenName=home -Pversion=1.0.0
 * ```
 */
class KetoyDevPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // 1. Register the extension
        val extension = project.extensions.create("ketoyDev", KetoyDevExtension::class.java)

        // 2. After evaluation, resolve from local.properties / project properties / defaults
        project.afterEvaluate {
            resolveFromProperties(project, extension)
        }

        // 3. Register all tasks
        project.tasks.register("ketoyPush", KetoyPushTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Upload a screen JSON from the screens directory to the Ketoy cloud server"
            task.extension = extension
        }

        project.tasks.register("ketoyPushAll", KetoyPushAllTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Upload all screen JSONs from the screens directory to the Ketoy cloud server"
            task.extension = extension
        }

        project.tasks.register("ketoyListScreens", KetoyListScreensTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "List all screens deployed on the Ketoy cloud server for this app"
            task.extension = extension
        }

        project.tasks.register("ketoyScreenVersions", KetoyScreenVersionsTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "List all versions of a screen on the Ketoy cloud server"
            task.extension = extension
        }

        project.tasks.register("ketoyScreenDetails", KetoyScreenDetailsTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Get details of a screen including its JSON content"
            task.extension = extension
        }

        project.tasks.register("ketoyRollback", KetoyRollbackTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Rollback a screen to a previous version on the cloud server"
            task.extension = extension
        }

        project.tasks.register("ketoyDeleteScreen", KetoyDeleteScreenTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Delete a screen and all its versions from the cloud server"
            task.extension = extension
        }
    }

    /**
     * Resolve extension values from Gradle project properties (`-P` flags)
     * and `local.properties` when they haven't been set explicitly in
     * the `ketoyDev {}` block. Falls back to built-in defaults for
     * `baseUrl` and `screensDir`.
     */
    private fun resolveFromProperties(project: Project, ext: KetoyDevExtension) {
        val localProps = loadLocalProperties(project)

        if (!ext.apiKey.isPresent) {
            val value = project.findProperty("ketoyApiKey") as? String
                ?: localProps.getProperty("KETOY_DEVELOPER_API_KEY")
            if (value != null) ext.apiKey.set(value)
        }

        if (!ext.packageName.isPresent) {
            val value = project.findProperty("ketoyPackageName") as? String
                ?: localProps.getProperty("KETOY_PACKAGE_NAME")
            if (value != null) ext.packageName.set(value)
        }

        if (!ext.baseUrl.isPresent) {
            val value = project.findProperty("ketoyBaseUrl") as? String
                ?: localProps.getProperty("KETOY_BASE_URL")
                ?: DEFAULT_BASE_URL
            ext.baseUrl.set(value.trimEnd('/'))
        }

        if (!ext.screensDir.isPresent) {
            val value = project.findProperty("ketoyScreensDir") as? String
                ?: localProps.getProperty("KETOY_SCREENS_DIR")
                ?: DEFAULT_SCREENS_DIR
            ext.screensDir.set(value)
        }
    }

    private fun loadLocalProperties(project: Project): Properties {
        val props = Properties()
        val file = project.rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { props.load(it) }
        }
        return props
    }

    companion object {
        const val TASK_GROUP = "ketoy"
        const val DEFAULT_BASE_URL = "https://api.ketoy.dev"
        const val DEFAULT_SCREENS_DIR = "ketoy-screens"
    }
}
