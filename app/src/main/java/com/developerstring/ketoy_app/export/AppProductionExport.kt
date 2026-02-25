package com.developerstring.ketoy_app.export

import com.developerstring.ketoy.export.KetoyProductionExport
import com.developerstring.ketoy_app.screens.*

/**
 * Production screen and navigation export for the Ketoy demo app.
 *
 * This class declares **all** exportable screens and navigation graphs
 * for production use. Unlike [com.developerstring.ketoy_app.ExportScreensTest],
 * this export:
 *
 * - Uses **no test data** — no "Test User", no hardcoded balances
 * - Uses **no dummy transactions** — empty lists or realistic structure
 * - Produces JSON ready for **runtime consumption** via Cloud, assets, or CDN
 *
 * ## How it works
 *
 * Each screen builder is called with **empty / structural defaults** that
 * represent the shape of the UI. At runtime the app either:
 *
 * 1. **Renders from the DSL directly** — the `nodeBuilder` in `KetoyContent`
 *    produces a fresh tree with real dynamic data (Cloud or DSL fallback).
 * 2. **Fetches the JSON from Cloud** — the JSON exported here is pushed to
 *    Ketoy Cloud via `./gradlew ketoyPushAll`, and the SDK fetches it at runtime.
 *    Dynamic data is injected via `KetoyVariable` or `KComponent` props.
 * 3. **Loads from bundled assets** — copy `ketoy-export/` into `assets/`
 *    and call `KetoyProductionNavLoader.loadAllFromAssetsComplete(context, "ketoy-export")`.
 *
 * ## Run
 * ```
 * ./gradlew ketoyExportProd
 * ```
 *
 * Output: `ketoy-export/` at the project root.
 */
class AppProductionExport : KetoyProductionExport() {

    // ── Screens ─────────────────────────────────────────────────

    override fun registerScreens() {

        // ── Home Screen (multi-content) ─────────────────────
        // The Home screen has two KetoyContent blocks:
        //   "cards" — header, balance card, quick actions, income stat
        //   "transactions" — recent transaction list
        screen("home", displayName = "Home", description = "Main dashboard with balance and transactions") {
            content("cards") {
                buildHomeCards(
                    userName = "{{data:user:name}}",
                    totalBalance = "{{data:user:totalBalance}}",
                    income = "{{data:user:income}}",
                    notificationCount = 0,
                    isDark = false
                )
            }
            content("transactions") {
                buildHomeTransactions(
                    transactions = emptyList(),
                    isDark = false
                )
            }
        }

        // ── Profile Screen (single content) ─────────────────
        screen("profile", displayName = "Profile", description = "User profile and settings") {
            content {
                buildProfileScreen(
                    userName = "{{data:user:name}}",
                    isDark = false
                )
            }
        }

        // ── Analytics Screen (single content) ───────────────
        screen("analytics", displayName = "Analytics", description = "Income, expenses, and spending breakdown") {
            content {
                buildAnalyticsScreen(
                    income = "{{data:analytics:income}}",
                    expenses = "{{data:analytics:expenses}}",
                    savings = "{{data:analytics:savings}}",
                    isDark = false
                )
            }
        }

        // ── Cards Screen (single content) ───────────────────
        screen("cards", displayName = "Cards", description = "Wallet cards and card management") {
            content {
                buildCardsScreen(
                    selectedCardIndex = 0,
                    isDark = false
                )
            }
        }

        // ── History Screen (single content) ─────────────────
        screen("history_screen", displayName = "History", description = "Full transaction history") {
            content {
                buildHistoryScreen(
                    transactions = emptyList(),
                    isDark = false
                )
            }
        }
    }

    // ── Navigation Graphs ───────────────────────────────────────

    override fun registerNavGraphs() {
        // Main app navigation (bottom bar + drawer)
        navGraph(AppNavGraphs.main)

        // Demo nested navigation
        navGraph(AppNavGraphs.demo)
    }
}
