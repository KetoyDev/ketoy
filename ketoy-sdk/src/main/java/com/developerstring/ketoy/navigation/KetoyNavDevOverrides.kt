package com.developerstring.ketoy.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

/**
 * Holds live-reloadable navigation graph overrides from the Ketoy dev server.
 *
 * The [KetoyDevWrapper] populates this singleton when it receives `nav_*.json`
 * updates over the WebSocket connection. [KetoyNavHost] observes these overrides
 * using Compose's [SnapshotStateMap] and dynamically adds/removes/reorders
 * destinations without restarting the app.
 *
 * This is the **highest priority** override layer—when a dev-server override
 * is present, it takes precedence over both cloud overrides and local registry.
 *
 * ### Override priority (highest → lowest)
 * 1. **[KetoyNavDevOverrides]** — live dev-server reload (development only)
 * 2. [KetoyCloudNavOverrides] — cloud-fetched graphs
 * 3. [KetoyNavRegistry] — compile-time / locally registered graphs
 *
 * ### Usage
 * ```kotlin
 * // Typically populated automatically by KetoyDevWrapper.
 * // Manual override for testing:
 * KetoyNavDevOverrides.set("main", devNavGraph)
 *
 * // Clear on dev server disconnect:
 * KetoyNavDevOverrides.clearAll()
 *
 * // Check if an override is active:
 * val override = KetoyNavDevOverrides.get("main")
 * ```
 *
 * @see KetoyNavHost
 * @see KetoyNavGraph
 * @see KetoyCloudNavOverrides
 * @see KetoyNavRegistry
 */
object KetoyNavDevOverrides {

    /**
     * Observable map of `navHostName → live` [KetoyNavGraph] override from the dev server.
     *
     * Backed by [SnapshotStateMap] so Compose recomposes automatically when
     * entries change. When an entry is present for a given `navHostName`,
     * [KetoyNavHost] uses this nav graph instead of compile-time destinations
     * or cloud overrides.
     */
    val overrides: SnapshotStateMap<String, KetoyNavGraph> = androidx.compose.runtime.mutableStateMapOf()

    /**
     * Set or clear a dev-server navigation graph override for a specific nav host.
     *
     * Pass `null` to remove an existing override and fall back to
     * [KetoyCloudNavOverrides] or [KetoyNavRegistry].
     *
     * @param navHostName The name of the nav host to override (must match [KetoyNavHost]'s `navHostName`).
     * @param graph       The dev-server [KetoyNavGraph] to apply, or `null` to remove the override.
     * @see get
     * @see clearAll
     */
    fun set(navHostName: String, graph: KetoyNavGraph?) {
        if (graph != null) {
            overrides[navHostName] = graph
        } else {
            overrides.remove(navHostName)
        }
    }

    /**
     * Retrieve the dev-server override for a specific nav host.
     *
     * @param navHostName The name of the nav host to look up.
     * @return The dev-server [KetoyNavGraph], or `null` if no override is active.
     */
    fun get(navHostName: String): KetoyNavGraph? = overrides[navHostName]

    /**
     * Clear all dev-server navigation graph overrides.
     *
     * Typically called when the dev server disconnects, allowing
     * [KetoyNavHost] to fall back to cloud or local registry graphs.
     */
    fun clearAll() {
        overrides.clear()
    }
}
