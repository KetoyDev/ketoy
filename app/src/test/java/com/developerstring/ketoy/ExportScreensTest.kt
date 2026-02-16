package com.developerstring.ketoy

import com.developerstring.ketoy.screen.KetoyScreen
import com.developerstring.ketoy.screens.*
import org.junit.Test
import java.io.File

/**
 * Automatically exports all Ketoy screen DSL fallbacks to JSON.
 *
 * Run via:
 *   ./gradlew ketoyExport
 *
 * Or directly:
 *   ./gradlew :app:testDebugUnitTest --tests "*.ExportScreensTest"
 *
 * The exported JSON files land in `ketoy-screens/` at the project root,
 * which is the directory the dev server watches by default.
 *
 * Each `@KScreen` annotated composable has a corresponding `buildXxxUI()`
 * function used here for export — **no manual AppScreens list needed**.
 */
class ExportScreensTest {

    private val outputDir = File(System.getProperty("user.dir") ?: ".").resolve("../ketoy-screens")

    private val screens = listOf(
        KetoyScreen.fromNode("home", displayName = "Home") {
            buildHomeUI(toast = {}, isDark = true)
        },
        KetoyScreen.fromNode("profile", displayName = "Profile") {
            buildProfileUI(toast = {}, isDark = true)
        },
        KetoyScreen.fromNode("analytics", displayName = "Analytics") {
            buildAnalyticsUI(toast = {}, isDark = true)
        },
        KetoyScreen.fromNode("cards", displayName = "Cards") {
            buildCardsUI(toast = {}, isDark = true)
        },
    )

    @Test
    fun exportAllScreens() {
        outputDir.mkdirs()

        var exported = 0
        screens.forEach { screen ->
            val json = screen.buildExportJson()
            if (json != null) {
                val file = File(outputDir, "${screen.screenName}.json")
                file.writeText(json)
                println("\uD83D\uDCC4 Exported: ${screen.screenName} \u2192 ${file.absolutePath} (${json.length} bytes)")
                exported++
            } else {
                println("\u26A0\uFE0F  Skipped: ${screen.screenName} (no DSL builder)")
            }
        }

        println("\u2705 Exported $exported screen(s) to ${outputDir.absolutePath}")
    }
}
