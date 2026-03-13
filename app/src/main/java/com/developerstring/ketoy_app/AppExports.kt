package com.developerstring.ketoy_app

import com.developerstring.ketoy_app.screens.*

/**
 * App export manifest — triggers class-loading for all screen and nav
 * export definitions.
 *
 * Each screen file declares its export inline via `ketoyExport(...)`,
 * and each nav graph file declares its export via `ketoyNavExport(...)`.
 * Those top-level vals self-register with [com.developerstring.ketoy.export.KetoyExportRegistry]
 * when their containing file is class-loaded.
 *
 * This object simply **references** them all to ensure they're loaded
 * before the export runner executes.
 *
 * ## Adding a new screen
 *
 * 1. In your screen file, add a `ketoyExport(...)` definition
 * 2. Reference it here in the [init] block
 *
 * That's it — no separate test files, no manual builders, no duplication.
 */
object AppExports {
    init {
        // ── Screen exports (triggers class-loading) ─────────
        homeExport
        profileExport
        analyticsExport
        cardsExport
        historyExport

        // ── Nav graph exports ───────────────────────────────
        mainNavExport
        demoNavExport
    }

    /** Call this to ensure all exports are registered. */
    fun ensureLoaded() { /* init block does the work */ }
}
