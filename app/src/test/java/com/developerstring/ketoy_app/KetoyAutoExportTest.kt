package com.developerstring.ketoy_app

import com.developerstring.ketoy.export.KetoyAutoExportRunner
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Automatic screen and navigation export test.
 *
 * Replaces the old `ExportScreensTest` and `ProductionExportTest` with
 * a single, zero-boilerplate test that reads all export definitions
 * from [com.developerstring.ketoy.export.KetoyExportRegistry] and writes JSON files.
 *
 * ## How it works
 *
 * 1. Screen files declare their exports inline via `ketoyExport(...)`:
 *    ```kotlin
 *    val profileExport = ketoyExport("profile") {
 *        content { buildProfileScreen(userName = KData.user("name")) }
 *    }
 *    ```
 *
 * 2. [AppExports] references all export vals, triggering class-loading.
 *
 * 3. This test invokes [KetoyAutoExportRunner] which reads the registry
 *    and writes all JSON files.
 *
 * ## Gradle tasks
 *
 * ```bash
 * ./gradlew ketoyExport       # → ketoy-screens/  (dev server)
 * ./gradlew ketoyExportProd   # → ketoy-export/    (production)
 * ```
 *
 * Both tasks run this single test class — the only difference is the
 * output directory (controlled by the task, not the test).
 */
class KetoyAutoExportTest {

    private val runner = KetoyAutoExportRunner()

    @Before
    fun setUp() {
        // Ensure all screen and nav graph exports are registered.
        // The top-level `ketoyExport(...)` and `ketoyNavExport(...)` vals
        // self-register when their files are class-loaded. AppExports
        // references them all to guarantee loading.
        AppExports.ensureLoaded()
    }

    @Test
    fun exportForDevServer() {
        val outputDir = File(System.getProperty("user.dir") ?: ".").resolve("../ketoy-screens")
        val result = runner.exportAllWire(
            outputDir = outputDir,
            writeManifests = false  // dev server doesn't need manifests
        )
        println(result)
    }

    @Test
    fun exportForProduction() {
        val outputDir = File(System.getProperty("user.dir") ?: ".").resolve("../ketoy-export")
        val result = runner.exportAllWire(
            outputDir = outputDir,
            writeManifests = true
        )
        println(result)
    }
}
