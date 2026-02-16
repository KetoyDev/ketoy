package com.developerstring.ketoy.registry

import android.util.Log
import com.developerstring.ketoy.model.KComponentInfo
import com.developerstring.ketoy.model.KComponentMetadata

/**
 * Global registry for custom Ketoy components.
 *
 * Components are registered with a [KComponentInfo] that carries an
 * optional renderer lambda. The renderer is invoked at render time
 * when a JSON node references the component by name.
 *
 * ## Registering a Jetpack Compose composable
 *
 * ```kotlin
 * // Register with typed parameter extraction
 * KComponentRegistry.register(
 *     name = "UserCard",
 *     renderer = { props ->
 *         val name = props["name"] as? String ?: ""
 *         val age  = props["age"]  as? Int ?: 0
 *         val isVip = props["isVip"] as? Boolean ?: false
 *         UserCardComposable(name = name, age = age, isVip = isVip)
 *     },
 *     parameterTypes = mapOf(
 *         "name" to "String",
 *         "age"  to "Int",
 *         "isVip" to "Boolean"
 *     )
 * )
 * ```
 *
 * Then in JSON:
 * ```json
 * { "type": "UserCard", "props": { "name": "Alice", "age": 30, "isVip": true } }
 * ```
 */
object KComponentRegistry {

    private const val TAG = "KComponentRegistry"

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
        Log.d(TAG, "Registered component '${info.name}'")
    }

    /**
     * Register a custom Jetpack Compose composable for server-driven rendering.
     *
     * @param name           The JSON `"type"` value that maps to this composable.
     * @param renderer       A @Composable lambda that receives extracted properties as
     *                       `Map<String, Any>`. Values are automatically converted from
     *                       JSON primitives (String, Int, Float, Double, Boolean).
     * @param parameterTypes Optional map of parameter names to their type names
     *                       (for documentation / schema generation).
     * @param packageName    Optional package name (for metadata).
     * @param className      Optional class name (for metadata).
     */
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

    /**
     * Attempt to load a component from its metadata using reflection.
     *
     * Looks for a class matching [KComponentMetadata.packageName].[KComponentMetadata.className]
     * that has a static `register()` method. If found, invokes it to self-register the component.
     *
     * @return `true` if the component is now available, `false` otherwise.
     */
    fun loadFromMetadata(metadata: KComponentMetadata): Boolean {
        if (isAvailable(metadata.name)) return true

        // Attempt reflection-based loading
        val fqn = if (metadata.packageName.isNotEmpty() && metadata.className.isNotEmpty()) {
            "${metadata.packageName}.${metadata.className}"
        } else null

        if (fqn != null) {
            try {
                val clazz = Class.forName(fqn)
                // Look for a static register() method
                val registerMethod = clazz.getDeclaredMethod("register")
                registerMethod.invoke(null)
                if (isAvailable(metadata.name)) {
                    Log.d(TAG, "Loaded component '${metadata.name}' via reflection from $fqn")
                    return true
                }
            } catch (e: ClassNotFoundException) {
                Log.w(TAG, "Component class not found: $fqn")
            } catch (e: NoSuchMethodException) {
                Log.w(TAG, "No register() method in: $fqn")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load component '${metadata.name}' from $fqn: ${e.message}")
            }
        }

        Log.w(TAG, "Could not load component '${metadata.name}' from metadata")
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
