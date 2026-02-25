package com.developerstring.ketoy.navigation

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

/**
 * Holds cloud-fetched navigation graph overrides for [KetoyNavHost].
 *
 * When Ketoy Cloud is enabled, navigation graphs fetched from the remote server
 * are stored in this singleton. [KetoyNavHost] observes these overrides using
 * Compose's [SnapshotStateMap] and recomposes automatically when cloud nav data
 * arrives or changes.
 *
 * You typically do not interact with this object directly. Use
 * [KetoyCloudNavService][com.developerstring.ketoy.cloud.KetoyCloudNavService]
 * to fetch nav graphs; they are auto-registered here upon successful fetch.
 *
 * ### Override priority (highest → lowest)
 * 1. [KetoyNavDevOverrides] — live dev-server reload (development only)
 * 2. **[KetoyCloudNavOverrides]** — cloud-fetched graphs (production)
 * 3. [KetoyNavRegistry] — compile-time / locally registered graphs
 *
 * ### Usage
 * ```kotlin
 * // Cloud service triggers auto-registration:
 * KetoyCloudNavService.fetchNavGraph("nav_main")
 * // KetoyNavHost with navHostName="main" will recompose with the cloud graph.
 *
 * // Manual override (advanced):
 * KetoyCloudNavOverrides.set("main", cloudNavGraph)
 *
 * // Clear all cloud overrides:
 * KetoyCloudNavOverrides.clearAll()
 * ```
 *
 * @see KetoyNavHost
 * @see KetoyNavGraph
 * @see KetoyNavDevOverrides
 * @see KetoyNavRegistry
 */
object KetoyCloudNavOverrides {

    /**
     * Observable map of `navHostName → cloud-fetched` [KetoyNavGraph].
     *
     * Backed by [SnapshotStateMap] so Compose recomposes automatically when
     * entries change. When a graph is present for a given `navHostName`,
     * [KetoyNavHost] uses it instead of [KetoyNavRegistry].
     *
     * Dev-server overrides ([KetoyNavDevOverrides]) still take top priority.
     */
    val overrides: SnapshotStateMap<String, KetoyNavGraph> = mutableStateMapOf()

    /**
     * Set or remove a cloud navigation graph for a specific nav host.
     *
     * Pass `null` to remove an existing override and fall back to
     * [KetoyNavRegistry] (unless a dev-server override is active).
     *
     * @param navHostName The name of the nav host to override (must match [KetoyNavHost]'s `navHostName`).
     * @param graph       The cloud [KetoyNavGraph] to apply, or `null` to remove the override.
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
     * Retrieve the cloud override for a specific nav host.
     *
     * @param navHostName The name of the nav host to look up.
     * @return The cloud [KetoyNavGraph], or `null` if no cloud override is active.
     */
    fun get(navHostName: String): KetoyNavGraph? = overrides[navHostName]

    /**
     * Clear all cloud navigation graph overrides.
     *
     * After clearing, [KetoyNavHost] will fall back to [KetoyNavRegistry]
     * for all nav hosts (unless dev-server overrides are active).
     */
    fun clearAll() {
        overrides.clear()
    }
}
