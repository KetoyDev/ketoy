package com.developerstring.ketoy_app

import com.developerstring.ketoy_app.export.AppProductionExport
import org.junit.Test
import java.io.File

/**
 * Production export runner — generates deployment-ready JSON for
 * screens and navigation graphs.
 *
 * **This is NOT a test** in the traditional sense — it's a Gradle-triggered
 * export task that produces the `ketoy-export/` directory at the project root.
 *
 * ## Run
 * ```
 * ./gradlew ketoyExportProd
 * ```
 *
 * ## Output structure
 * ```
 * ketoy-export/
 * ├── home.json                     # Screen JSONs (one per screen)
 * ├── profile.json
 * ├── analytics.json
 * ├── cards.json
 * ├── history_screen.json
 * ├── nav_main.json                 # Individual nav graph JSONs
 * ├── nav_demo.json
 * ├── navigation_manifest.json      # Combined nav manifest (all graphs)
 * └── screen_manifest.json          # Screen index (names, contents, versions)
 * ```
 *
 * ## How this differs from `ExportScreensTest`
 *
 * | Aspect               | `ExportScreensTest`           | `ProductionExportTest`            |
 * |----------------------|-------------------------------|-----------------------------------|
 * | **Purpose**          | Dev-server hot-reload         | Production deployment             |
 * | **Output dir**       | `ketoy-screens/`              | `ketoy-export/`                   |
 * | **Data**             | Hardcoded test values          | Template variables / empty lists  |
 * | **Theme**            | Always `isDark = true`         | `isDark = false` (system default) |
 * | **Nav manifest**     | No                             | Yes (combined + individual)       |
 * | **Screen manifest**  | No                             | Yes (index of all screens)        |
 * | **Gradle task**      | `ketoyExport`                  | `ketoyExportProd`                 |
 *
 * ## Consuming the export in a production app
 *
 * ### Option 1: Bundle in assets
 * Copy `ketoy-export/` into `app/src/main/assets/ketoy-export/` and at startup:
 * ```kotlin
 * KetoyProductionNavLoader.loadAllFromAssetsComplete(context, "ketoy-export")
 * ```
 *
 * ### Option 2: Push to Ketoy Cloud
 * ```
 * ./gradlew ketoyExportProd
 * ./gradlew ketoyPushAll -Pversion=1.0.0 -PscreensDir=ketoy-export
 * ```
 *
 * ### Option 3: Serve from CDN / custom HTTP endpoint
 * Deploy the files to any static host. The SDK fetches them at runtime
 * via cloud config or manual `KetoyScreen.fromJson()`.
 */
class ProductionExportTest {

    private val outputDir = File(System.getProperty("user.dir") ?: ".").resolve("../ketoy-export")

    @Test
    fun exportProduction() {
        println()
        println("╔════════════════════════════════════════════╗")
        println("║     Ketoy Production Export                ║")
        println("╚════════════════════════════════════════════╝")
        println()

        val exporter = AppProductionExport()
        val result = exporter.buildExport()

        // Write all screen and nav graph JSONs
        val summary = result.writeTo(outputDir)
        println(summary)

        // Write navigation manifest (all nav graphs in one file)
        if (result.navGraphs.isNotEmpty()) {
            val manifestFile = File(outputDir, "navigation_manifest.json")
            manifestFile.writeText(result.buildNavigationManifest())
            println("  📋 Navigation manifest → ${manifestFile.name} (${result.navGraphs.size} graphs)")
        }

        // Write screen manifest (index of all screens)
        if (result.screens.isNotEmpty()) {
            val screenManifestFile = File(outputDir, "screen_manifest.json")
            screenManifestFile.writeText(result.buildScreenManifest(exporter.screens))
            println("  📋 Screen manifest → ${screenManifestFile.name} (${result.screens.size} screens)")
        }

        // Print individual export details
        println()
        println("  ── Screens ────────────────────────────────")
        result.screens.forEach { export ->
            println("  📄 ${export.screenName} → ${export.fileName} (${export.json.length} bytes)")
        }

        println()
        println("  ── Navigation ─────────────────────────────")
        result.navGraphs.forEach { export ->
            println("  🗺️  ${export.navHostName} → ${export.fileName} (${export.destinationCount} destinations, ${export.navigationCount} actions)")
        }

        println()
        println("  ✅ Production export complete: ${outputDir.absolutePath}")
        println()
    }
}
