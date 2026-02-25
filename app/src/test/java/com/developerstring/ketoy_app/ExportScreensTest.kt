package com.developerstring.ketoy_app

import com.developerstring.ketoy.navigation.KetoyNavRegistry
import com.developerstring.ketoy.screen.KetoyScreen
import com.developerstring.ketoy_app.screens.*
import org.junit.Test
import java.io.File

/**
 * Automatically exports all Ketoy screen DSL fallbacks to JSON,
 * **and** navigation graphs to `nav_*.json` files.
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
 * Each `@KScreen` annotated composable has a corresponding `buildXxxScreen()`
 * DSL builder used here for export.
 */
class ExportScreensTest {

    private val outputDir = File(System.getProperty("user.dir") ?: ".").resolve("../ketoy-screens")

    // ── Screen definitions ──────────────────────────────────────

    private val screens = listOf(
        // Home screen has multiple KetoyContent blocks (cards + transactions)
        KetoyScreen(screenName = "home", displayName = "Home").apply {
            addContent(name = "cards", nodeBuilder = {
                buildHomeCards(
                    userName = "Test User",
                    totalBalance = "\$12,450.00",
                    income = "\$4,200.00",
                    notificationCount = 3,
                    isDark = true
                )
            })
            addContent(name = "transactions", nodeBuilder = {
                buildHomeTransactions(
                    transactions = listOf(
                        Triple("Salary", "Today", "+\$4,200.00"),
                        Triple("Groceries", "Yesterday", "-\$85.50"),
                        Triple("Netflix", "Feb 1", "-\$15.99"),
                    ),
                    isDark = true
                )
            })
        },
        KetoyScreen.fromNode("profile", displayName = "Profile") {
            buildProfileScreen(userName = "Test User", isDark = true)
        },
        KetoyScreen.fromNode("analytics", displayName = "Analytics") {
            buildAnalyticsScreen(
                income = "\$4,200.00",
                expenses = "\$2,150.00",
                savings = "\$2,050.00",
                isDark = true
            )
        },
        KetoyScreen.fromNode("cards", displayName = "Cards") {
            buildCardsScreen(selectedCardIndex = 0, isDark = true)
        },
        KetoyScreen.fromNode("history_screen", displayName = "History") {
            buildHistoryScreen(
                transactions = listOf(
                    Triple("Salary", "Today", "+\$4,200.00"),
                    Triple("Groceries", "Yesterday", "-\$85.50"),
                    Triple("Netflix", "Feb 1", "-\$15.99"),
                ),
                isDark = true
            )
        },
    )

    // ── Export ───────────────────────────────────────────────────

    @Test
    fun exportAllScreens() {
        outputDir.mkdirs()

        // Export screens
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

        // Register all nav graphs from app code (main + demo)
        AppNavGraphs.registerAll()
        val allNavGraphs = KetoyNavRegistry.getAll().values

        // Export navigation graphs
        var navExported = 0
        allNavGraphs.forEach { graph ->
            val json = graph.toJson()
            val file = File(outputDir, "nav_${graph.navHostName}.json")
            file.writeText(json)
            println("\uD83D\uDDFA\uFE0F  Nav exported: ${graph.navHostName} \u2192 ${file.absolutePath} (${json.length} bytes, ${graph.destinations.size} destinations)")
            navExported++
        }

        println("\u2705 Exported $exported screen(s) + $navExported nav graph(s) to ${outputDir.absolutePath}")
    }
}
