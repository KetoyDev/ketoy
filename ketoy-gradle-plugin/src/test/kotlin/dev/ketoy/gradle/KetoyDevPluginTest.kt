package dev.ketoy.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Unit tests for [KetoyDevPlugin].
 *
 * These tests verify that the plugin applies correctly, registers all expected
 * tasks with the right group/description, and configures the extension properly.
 */
class KetoyDevPluginTest {

    @Test
    fun `plugin applies without error`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        assertNotNull(project.plugins.findPlugin(KetoyDevPlugin::class.java))
    }

    @Test
    fun `extension is registered with correct name`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val extension = project.extensions.findByName("ketoyDev")
        assertNotNull(extension, "Extension 'ketoyDev' should be registered")
        assertInstanceOf(KetoyDevExtension::class.java, extension)
    }

    @Test
    fun `extension has no defaults before evaluation`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val extension = project.extensions.getByType(KetoyDevExtension::class.java)

        // Before afterEvaluate, no values are pre-set (defaults applied in afterEvaluate)
        assertFalse(extension.apiKey.isPresent, "apiKey should not have a default")
        assertFalse(extension.packageName.isPresent, "packageName should not have a default")
        assertFalse(extension.baseUrl.isPresent, "baseUrl resolved in afterEvaluate")
        assertFalse(extension.screensDir.isPresent, "screensDir resolved in afterEvaluate")
    }

    @Test
    fun `extension values can be set`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val extension = project.extensions.getByType(KetoyDevExtension::class.java)
        extension.apiKey.set("test-key-123")
        extension.packageName.set("com.example.test")
        extension.baseUrl.set("https://custom.api.ketoy.dev")
        extension.screensDir.set("my-screens")

        assertEquals("test-key-123", extension.apiKey.get())
        assertEquals("com.example.test", extension.packageName.get())
        assertEquals("https://custom.api.ketoy.dev", extension.baseUrl.get())
        assertEquals("my-screens", extension.screensDir.get())
    }

    // ── Task registration tests ──────────────────────────────────

    @Test
    fun `ketoyPush task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyPush")
        assertNotNull(task, "Task 'ketoyPush' should be registered")
        assertEquals("ketoy", task!!.group)
        assertTrue(task.description!!.contains("Upload"), "Description should mention upload")
    }

    @Test
    fun `ketoyPushAll task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyPushAll")
        assertNotNull(task, "Task 'ketoyPushAll' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyListScreens task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyListScreens")
        assertNotNull(task, "Task 'ketoyListScreens' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyScreenVersions task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyScreenVersions")
        assertNotNull(task, "Task 'ketoyScreenVersions' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyScreenDetails task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyScreenDetails")
        assertNotNull(task, "Task 'ketoyScreenDetails' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyRollback task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyRollback")
        assertNotNull(task, "Task 'ketoyRollback' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyDeleteScreen task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyDeleteScreen")
        assertNotNull(task, "Task 'ketoyDeleteScreen' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `all cloud tasks are registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val expectedTasks = listOf(
            "ketoyPush", "ketoyPushAll", "ketoyListScreens",
            "ketoyScreenVersions", "ketoyScreenDetails",
            "ketoyRollback", "ketoyDeleteScreen"
        )

        expectedTasks.forEach { taskName ->
            assertNotNull(
                project.tasks.findByName(taskName),
                "Task '$taskName' should be registered"
            )
        }
    }

    @Test
    fun `all tasks belong to ketoy group`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val ketoyTasks = project.tasks.filter { it.group == "ketoy" }
        assertEquals(11, ketoyTasks.size, "Should have exactly 11 tasks in the 'ketoy' group")
    }

    // ── Export task registration tests ───────────────────────────

    @Test
    fun `ketoyExport task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyExport")
        assertNotNull(task, "Task 'ketoyExport' should be registered")
        assertEquals("ketoy", task!!.group)
        assertTrue(task.description!!.contains("Export"), "Description should mention export")
    }

    @Test
    fun `ketoyExportProd task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyExportProd")
        assertNotNull(task, "Task 'ketoyExportProd' should be registered")
        assertEquals("ketoy", task!!.group)
        assertTrue(task.description!!.contains("production", ignoreCase = true), "Description should mention production")
    }

    // ── Server task registration tests ──────────────────────────

    @Test
    fun `ketoyServe task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyServe")
        assertNotNull(task, "Task 'ketoyServe' should be registered")
        assertEquals("ketoy", task!!.group)
        assertTrue(task.description!!.contains("Dev Server"), "Description should mention Dev Server")
    }

    @Test
    fun `ketoyDev task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val task = project.tasks.findByName("ketoyDev")
        assertNotNull(task, "Task 'ketoyDev' should be registered")
        assertEquals("ketoy", task!!.group)
        assertTrue(task.description!!.contains("auto-export"), "Description should mention auto-export")
    }

    // ── Extension defaults (export & server) ────────────────────

    @Test
    fun `extension export defaults are resolved after evaluation`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        // Trigger afterEvaluate callbacks
        (project as org.gradle.api.internal.project.ProjectInternal).evaluate()

        val extension = project.extensions.getByType(KetoyDevExtension::class.java)

        assertEquals("KetoyAutoExportTest", extension.exportTestClass.get())
        assertEquals("ketoy-export", extension.prodExportDir.get())
        assertEquals("app", extension.appModule.get())
        assertEquals("testDebugUnitTest", extension.testTaskName.get())
        assertEquals(8484, extension.serverPort.get())
    }

    @Test
    fun `extension export values can be customised`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val extension = project.extensions.getByType(KetoyDevExtension::class.java)
        extension.exportTestClass.set("MyExportTest")
        extension.prodExportDir.set("my-export")
        extension.appModule.set("my-app")
        extension.testTaskName.set("testReleaseUnitTest")
        extension.serverPort.set(9090)

        assertEquals("MyExportTest", extension.exportTestClass.get())
        assertEquals("my-export", extension.prodExportDir.get())
        assertEquals("my-app", extension.appModule.get())
        assertEquals("testReleaseUnitTest", extension.testTaskName.get())
        assertEquals(9090, extension.serverPort.get())
    }

    @Test
    fun `all eleven tasks are registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.devtools")

        val expectedTasks = listOf(
            // Cloud tasks
            "ketoyPush", "ketoyPushAll", "ketoyListScreens",
            "ketoyScreenVersions", "ketoyScreenDetails",
            "ketoyRollback", "ketoyDeleteScreen",
            // Export & server tasks
            "ketoyExport", "ketoyExportProd",
            "ketoyServe", "ketoyDev"
        )

        expectedTasks.forEach { taskName ->
            assertNotNull(
                project.tasks.findByName(taskName),
                "Task '$taskName' should be registered"
            )
        }
    }
}
