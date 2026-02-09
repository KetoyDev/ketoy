package com.developerstring.ketoy.registry

import com.developerstring.ketoy.model.KComponentInfo
import com.developerstring.ketoy.model.KComponentMetadata

/**
 * Global registry for custom Ketoy components.
 *
 * Components are registered with a [KComponentInfo] that carries an
 * optional renderer lambda. The renderer is invoked at render time
 * when a JSON node references the component by name.
 */
object KComponentRegistry {

    private val components = mutableMapOf<String, KComponentInfo>()
    private val componentMetadata = mutableMapOf<String, KComponentMetadata>()
    private var isInitialized = false

    /* ── Initialisation ─────────────────────────────── */

    fun initialize() {
        if (isInitialized) return
        isInitialized = true
    }

    /* ── Registration ───────────────────────────────── */

    fun register(info: KComponentInfo) {
        components[info.name] = info
        componentMetadata[info.name] = KComponentMetadata(
            name = info.name,
            packageName = info.packageName,
            className = info.className,
            version = info.version
        )
    }

    fun register(
        name: String,
        renderer: @androidx.compose.runtime.Composable (Map<String, Any>) -> Unit,
        parameterTypes: Map<String, String> = emptyMap(),
        packageName: String = "",
        className: String = ""
    ) {
        register(
            KComponentInfo(
                name = name,
                packageName = packageName,
                className = className,
                parameterTypes = parameterTypes
            ).apply { this.renderer = renderer }
        )
    }

    /* ── Retrieval ───────────────────────────────────── */

    fun get(name: String): KComponentInfo? {
        if (!isInitialized) initialize()
        return components[name]
    }

    fun getAll(): Map<String, KComponentInfo> {
        if (!isInitialized) initialize()
        return components.toMap()
    }

    fun getMetadata(name: String): KComponentMetadata? = componentMetadata[name]

    fun getAllMetadata(): Map<String, KComponentMetadata> = componentMetadata.toMap()

    fun isAvailable(name: String): Boolean {
        if (!isInitialized) initialize()
        return components.containsKey(name)
    }

    /* ── Dynamic loading ────────────────────────────── */

    fun loadFromMetadata(metadata: KComponentMetadata): Boolean {
        if (isAvailable(metadata.name)) return true
        // Placeholder – production SDK could use reflection here
        return false
    }

    /* ── Lifecycle ───────────────────────────────────── */

    fun clear() {
        components.clear()
        componentMetadata.clear()
        isInitialized = false
    }

    fun reset() {
        clear()
        initialize()
    }
}
