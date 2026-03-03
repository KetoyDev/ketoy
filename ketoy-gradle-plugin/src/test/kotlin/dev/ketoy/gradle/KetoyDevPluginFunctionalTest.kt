package dev.ketoy.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional tests using Gradle TestKit.
 *
 * These tests spin up a real Gradle build in a temp directory
 * to verify the plugin works end-to-end.
 */
class KetoyDevPluginFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File
    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts").apply {
            writeText("""rootProject.name = "test-project"""")
        }
        buildFile = File(testProjectDir, "build.gradle.kts")
    }

    private fun runner(vararg args: String) = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments(*args)
        .withPluginClasspath()
        .forwardOutput()

    // ── Plugin application ───────────────────────────────────────

    @Test
    fun `plugin applies and tasks are listed`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            """.trimIndent()
        )

        val result = runner("tasks", "--group=ketoy", "--stacktrace").build()
        val output = result.output

        assertTrue(output.contains("ketoyPush"), "Should list ketoyPush")
        assertTrue(output.contains("ketoyPushAll"), "Should list ketoyPushAll")
        assertTrue(output.contains("ketoyListScreens"), "Should list ketoyListScreens")
        assertTrue(output.contains("ketoyScreenVersions"), "Should list ketoyScreenVersions")
        assertTrue(output.contains("ketoyScreenDetails"), "Should list ketoyScreenDetails")
        assertTrue(output.contains("ketoyRollback"), "Should list ketoyRollback")
        assertTrue(output.contains("ketoyDeleteScreen"), "Should list ketoyDeleteScreen")
    }

    // ── Extension configuration ──────────────────────────────────

    @Test
    fun `extension is configurable in build script`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }

            ketoyDev {
                apiKey.set("test-api-key")
                packageName.set("com.example.test")
                baseUrl.set("https://custom.api.ketoy.dev")
                screensDir.set("my-screens")
            }

            // Verification task to ensure extension was configured
            tasks.register("printConfig") {
                doLast {
                    val ext = project.extensions.getByType(dev.ketoy.gradle.KetoyDevExtension::class.java)
                    println("API_KEY=" + ext.apiKey.get())
                    println("PACKAGE=" + ext.packageName.get())
                    println("BASE_URL=" + ext.baseUrl.get())
                    println("SCREENS_DIR=" + ext.screensDir.get())
                }
            }
            """.trimIndent()
        )

        val result = runner("printConfig").build()

        assertTrue(result.output.contains("API_KEY=test-api-key"))
        assertTrue(result.output.contains("PACKAGE=com.example.test"))
        assertTrue(result.output.contains("BASE_URL=https://custom.api.ketoy.dev"))
        assertTrue(result.output.contains("SCREENS_DIR=my-screens"))
    }

    // ── local.properties fallback ────────────────────────────────

    @Test
    fun `falls back to local properties when extension not configured`() {
        File(testProjectDir, "local.properties").writeText(
            """
            KETOY_DEVELOPER_API_KEY=local-prop-key
            KETOY_PACKAGE_NAME=com.local.prop
            KETOY_BASE_URL=https://local.api.ketoy.dev
            """.trimIndent()
        )

        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }

            tasks.register("printConfig") {
                doLast {
                    val ext = project.extensions.getByType(dev.ketoy.gradle.KetoyDevExtension::class.java)
                    println("API_KEY=" + (ext.apiKey.orNull ?: "NOT_SET"))
                    println("PACKAGE=" + (ext.packageName.orNull ?: "NOT_SET"))
                    println("BASE_URL=" + ext.baseUrl.get())
                }
            }
            """.trimIndent()
        )

        val result = runner("printConfig", "--info").build()

        // If afterEvaluate resolves from local.properties, assertions pass;
        // otherwise the fallback values are used
        assertTrue(
            result.output.contains("API_KEY=local-prop-key"),
            "apiKey should be resolved from local.properties. Output: ${result.output.takeLast(500)}"
        )
        assertTrue(
            result.output.contains("PACKAGE=com.local.prop"),
            "packageName should be resolved from local.properties"
        )
        assertTrue(
            result.output.contains("BASE_URL=https://local.api.ketoy.dev"),
            "baseUrl should be resolved from local.properties"
        )
    }

    // ── Task failure messages (missing config) ───────────────────

    @Test
    fun `ketoyPush fails with missing apiKey`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        val result = runner("ketoyPush", "-PscreenName=home", "-Pversion=1.0.0")
            .buildAndFail()

        assertTrue(
            result.output.contains("apiKey") && result.output.contains("not configured"),
            "Should report missing apiKey configuration"
        )
    }

    @Test
    fun `ketoyPush fails with missing screenName`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        val result = runner("ketoyPush", "-Pversion=1.0.0").buildAndFail()

        assertTrue(
            result.output.contains("Missing -PscreenName"),
            "Should report missing screenName"
        )
    }

    @Test
    fun `ketoyPush fails with missing screen file`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        // Create the screens dir but not the specific file
        File(testProjectDir, "ketoy-screens").mkdirs()

        val result = runner("ketoyPush", "-PscreenName=nonexistent", "-Pversion=1.0.0")
            .buildAndFail()

        assertTrue(
            result.output.contains("Screen file not found"),
            "Should report missing screen file"
        )
    }

    @Test
    fun `ketoyPushAll fails with empty screens directory`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        File(testProjectDir, "ketoy-screens").mkdirs()

        val result = runner("ketoyPushAll", "-PscreenVersion=1.0.0").buildAndFail()

        assertTrue(
            result.output.contains("No JSON files found"),
            "Should report no JSON files found"
        )
    }

    @Test
    fun `ketoyPushAll fails with missing screenVersion`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        val result = runner("ketoyPushAll").buildAndFail()

        assertTrue(
            result.output.contains("Missing") && result.output.contains("version"),
            "Should report missing version"
        )
    }

    @Test
    fun `ketoyScreenVersions fails with missing screenName`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        val result = runner("ketoyScreenVersions").buildAndFail()

        assertTrue(
            result.output.contains("Missing -PscreenName"),
            "Should report missing screenName"
        )
    }

    @Test
    fun `ketoyScreenDetails fails with missing screenName`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        val result = runner("ketoyScreenDetails").buildAndFail()

        assertTrue(
            result.output.contains("Missing -PscreenName"),
            "Should report missing screenName"
        )
    }

    @Test
    fun `ketoyRollback fails with missing screenName`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        val result = runner("ketoyRollback", "-Pversion=1.0.0").buildAndFail()

        assertTrue(
            result.output.contains("Missing -PscreenName"),
            "Should report missing screenName"
        )
    }

    @Test
    fun `ketoyRollback fails with missing screenVersion`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        val result = runner("ketoyRollback", "-PscreenName=home").buildAndFail()

        assertTrue(
            result.output.contains("Missing") && result.output.contains("Version", ignoreCase = true),
            "Should report missing version"
        )
    }

    @Test
    fun `ketoyDeleteScreen fails with missing screenName`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }
            ketoyDev {
                apiKey.set("test-key")
                packageName.set("com.example.test")
            }
            """.trimIndent()
        )

        val result = runner("ketoyDeleteScreen").buildAndFail()

        assertTrue(
            result.output.contains("Missing -PscreenName"),
            "Should report missing screenName"
        )
    }

    // ── Extension DSL overrides project properties ───────────────

    @Test
    fun `extension DSL takes precedence over local properties`() {
        File(testProjectDir, "local.properties").writeText(
            """
            KETOY_DEVELOPER_API_KEY=local-key
            KETOY_PACKAGE_NAME=com.local.package
            """.trimIndent()
        )

        buildFile.writeText(
            """
            plugins {
                id("dev.ketoy.ketoy-dev")
            }

            ketoyDev {
                apiKey.set("dsl-key")
                packageName.set("com.dsl.package")
            }

            tasks.register("printConfig") {
                doLast {
                    val ext = project.extensions.getByType(dev.ketoy.gradle.KetoyDevExtension::class.java)
                    println("API_KEY=" + ext.apiKey.get())
                    println("PACKAGE=" + ext.packageName.get())
                }
            }
            """.trimIndent()
        )

        val result = runner("printConfig").build()

        assertTrue(result.output.contains("API_KEY=dsl-key"), "DSL should override local.properties")
        assertTrue(result.output.contains("PACKAGE=com.dsl.package"), "DSL should override local.properties")
    }
}
