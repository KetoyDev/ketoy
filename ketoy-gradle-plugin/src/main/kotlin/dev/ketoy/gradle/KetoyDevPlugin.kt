package dev.ketoy.gradle

import dev.ketoy.gradle.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import java.util.Properties

/**
 * Ketoy Dev — Gradle plugin for managing Ketoy SDUI screens.
 *
 * Registers the `ketoyDev {}` extension and the following tasks:
 *
 * ### Cloud tasks
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
 * ### Dev & Export tasks
 * | Task                    | Description                                        |
 * |-------------------------|----------------------------------------------------|
 * | `ketoyExport`           | Export DSL screens to JSON for the dev server      |
 * | `ketoyExportProd`       | Export production-ready screens + manifests         |
 * | `ketoyServe`            | Start the Ketoy Dev Server for hot-reload preview  |
 * | `ketoyDev`              | Start Dev Server with auto-export (edit → live)    |
 *
 * ## Usage
 * ```kotlin
 * plugins {
 *     id("dev.ketoy.devtools") version "0.1-beta"
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
 * ./gradlew ketoyExport
 * ./gradlew ketoyExportProd
 * ./gradlew ketoyServe
 * ./gradlew ketoyDev
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
            wireExportDependencies(project, extension)
        }

        // 3. Register cloud tasks
        registerCloudTasks(project, extension)

        // 4. Register export tasks
        registerExportTasks(project, extension)

        // 5. Register server tasks
        registerServerTasks(project, extension)

        // 6. Configure test filtering for export tasks
        configureExportTestFiltering(project, extension)
    }

    // ── Cloud tasks ──────────────────────────────────────────────

    private fun registerCloudTasks(project: Project, extension: KetoyDevExtension) {
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

    // ── Export tasks ─────────────────────────────────────────────

    private fun registerExportTasks(project: Project, extension: KetoyDevExtension) {
        project.tasks.register("ketoyExport", KetoyExportTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Export Ketoy DSL screens to JSON files for the dev server"
            task.extension = extension
        }

        project.tasks.register("ketoyExportProd", KetoyExportProdTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Export production-ready Ketoy screens and navigation to ketoy-export/"
            task.extension = extension
        }
    }

    /**
     * Wires [ketoyExport] and [ketoyExportProd] to the correct test task
     * in the app module. Called from `afterEvaluate` so that extension
     * values are fully resolved.
     */
    private fun wireExportDependencies(project: Project, ext: KetoyDevExtension) {
        val appModule = ext.appModule.getOrElse(DEFAULT_APP_MODULE)
        val testTask = ext.testTaskName.getOrElse(DEFAULT_TEST_TASK)
        val appProject = project.rootProject.findProject(":$appModule")

        if (appProject != null) {
            val fullTestTaskPath = ":$appModule:$testTask"
            project.tasks.named("ketoyExport").configure { it.dependsOn(fullTestTaskPath) }
            project.tasks.named("ketoyExportProd").configure { it.dependsOn(fullTestTaskPath) }
        } else {
            project.logger.warn(
                "Ketoy: App module ':$appModule' not found. " +
                "ketoyExport and ketoyExportProd tasks will not be wired to a test task. " +
                "Set ketoyDev { appModule.set(\"your-app-module\") } to configure."
            )
        }
    }

    /**
     * Configures the test task filter so that only the appropriate export
     * test method runs when `ketoyExport` or `ketoyExportProd` is in the
     * task graph. This prevents all other tests from running during an
     * export invocation.
     *
     * Both tasks use [KetoyAutoExportTest] — the single auto-export test
     * class. `ketoyExport` runs `exportForDevServer` and `ketoyExportProd`
     * runs `exportForProduction`.
     */
    private fun configureExportTestFiltering(project: Project, ext: KetoyDevExtension) {
        project.gradle.taskGraph.whenReady(org.gradle.api.Action { graph ->
            val appModule = ext.appModule.getOrElse(DEFAULT_APP_MODULE)
            val testTaskName = ext.testTaskName.getOrElse(DEFAULT_TEST_TASK)
            val fullTestTaskPath = ":$appModule:$testTaskName"

            val isExport = graph.hasTask(":${project.name}:ketoyExport") || graph.hasTask("ketoyExport")
            val isExportProd = graph.hasTask(":${project.name}:ketoyExportProd") || graph.hasTask("ketoyExportProd")

            if (isExport || isExportProd) {
                val exportTestClass = ext.exportTestClass.getOrElse(DEFAULT_EXPORT_TEST_CLASS)
                val testTasks: List<Test> = graph.allTasks.filterIsInstance<Test>()
                for (testTask in testTasks) {
                    if (testTask.path == fullTestTaskPath) {
                        // Force the test to always re-run when triggered by export
                        testTask.outputs.upToDateWhen { false }

                        if (isExport) {
                            testTask.filter(org.gradle.api.Action {
                                it.includeTestsMatching("*.$exportTestClass.exportForDevServer")
                            })
                        }
                        if (isExportProd) {
                            testTask.filter(org.gradle.api.Action {
                                it.includeTestsMatching("*.$exportTestClass.exportForProduction")
                            })
                        }
                    }
                }
            }
        })
    }

    // ── Server tasks ─────────────────────────────────────────────

    private fun registerServerTasks(project: Project, extension: KetoyDevExtension) {
        project.tasks.register("ketoyServe", KetoyServeTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Start the Ketoy Dev Server for hot-reload preview"
            task.extension = extension
        }

        project.tasks.register("ketoyDev", KetoyDevTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Start Ketoy Dev Server with auto-export (edit DSL → live app update)"
            task.extension = extension
        }
    }

    // ── Property resolution ──────────────────────────────────────

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

        if (!ext.appId.isPresent) {
            val value = project.findProperty("ketoyAppId") as? String
                ?: localProps.getProperty("KETOY_APP_ID")
            if (value != null) ext.appId.set(value)
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

        if (!ext.prodExportDir.isPresent) {
            val value = project.findProperty("ketoyProdExportDir") as? String
                ?: localProps.getProperty("KETOY_PROD_EXPORT_DIR")
                ?: DEFAULT_PROD_EXPORT_DIR
            ext.prodExportDir.set(value)
        }

        if (!ext.exportTestClass.isPresent) {
            val value = project.findProperty("ketoyExportTestClass") as? String
                ?: DEFAULT_EXPORT_TEST_CLASS
            ext.exportTestClass.set(value)
        }

        if (!ext.appModule.isPresent) {
            val value = project.findProperty("ketoyAppModule") as? String
                ?: DEFAULT_APP_MODULE
            ext.appModule.set(value)
        }

        if (!ext.testTaskName.isPresent) {
            val value = project.findProperty("ketoyTestTaskName") as? String
                ?: DEFAULT_TEST_TASK
            ext.testTaskName.set(value)
        }

        if (!ext.serverPort.isPresent) {
            val value = project.findProperty("ketoyServerPort") as? String
            ext.serverPort.set(value?.toIntOrNull() ?: DEFAULT_SERVER_PORT)
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
        const val DEFAULT_PROD_EXPORT_DIR = "ketoy-export"
        const val DEFAULT_EXPORT_TEST_CLASS = "KetoyAutoExportTest"
        const val DEFAULT_APP_MODULE = "app"
        const val DEFAULT_TEST_TASK = "testDebugUnitTest"
        const val DEFAULT_SERVER_PORT = 8484
    }
}
