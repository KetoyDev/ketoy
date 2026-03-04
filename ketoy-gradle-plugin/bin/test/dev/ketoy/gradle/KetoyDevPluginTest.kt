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
        project.plugins.apply("dev.ketoy.ketoy-dev")

        assertNotNull(project.plugins.findPlugin(KetoyDevPlugin::class.java))
    }

    @Test
    fun `extension is registered with correct name`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val extension = project.extensions.findByName("ketoyDev")
        assertNotNull(extension, "Extension 'ketoyDev' should be registered")
        assertInstanceOf(KetoyDevExtension::class.java, extension)
    }

    @Test
    fun `extension has no defaults before evaluation`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

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
        project.plugins.apply("dev.ketoy.ketoy-dev")

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
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val task = project.tasks.findByName("ketoyPush")
        assertNotNull(task, "Task 'ketoyPush' should be registered")
        assertEquals("ketoy", task!!.group)
        assertTrue(task.description!!.contains("Upload"), "Description should mention upload")
    }

    @Test
    fun `ketoyPushAll task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val task = project.tasks.findByName("ketoyPushAll")
        assertNotNull(task, "Task 'ketoyPushAll' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyListScreens task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val task = project.tasks.findByName("ketoyListScreens")
        assertNotNull(task, "Task 'ketoyListScreens' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyScreenVersions task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val task = project.tasks.findByName("ketoyScreenVersions")
        assertNotNull(task, "Task 'ketoyScreenVersions' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyScreenDetails task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val task = project.tasks.findByName("ketoyScreenDetails")
        assertNotNull(task, "Task 'ketoyScreenDetails' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyRollback task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val task = project.tasks.findByName("ketoyRollback")
        assertNotNull(task, "Task 'ketoyRollback' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `ketoyDeleteScreen task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val task = project.tasks.findByName("ketoyDeleteScreen")
        assertNotNull(task, "Task 'ketoyDeleteScreen' should be registered")
        assertEquals("ketoy", task!!.group)
    }

    @Test
    fun `all seven tasks are registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("dev.ketoy.ketoy-dev")

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
        project.plugins.apply("dev.ketoy.ketoy-dev")

        val ketoyTasks = project.tasks.filter { it.group == "ketoy" }
        assertEquals(7, ketoyTasks.size, "Should have exactly 7 tasks in the 'ketoy' group")
    }
}
