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
    /**
     * Initialise the component registry.
     *
     * This is called lazily on first [get] or [isAvailable] access.
     * Calling it explicitly is safe and idempotent — subsequent calls
     * are no-ops.
     */    fun initialize() {
        if (isInitialized) return
        isInitialized = true
    }

    /* ── Registration ───────────────────────────────── */

    /**
     * Register a component using a pre-built [KComponentInfo].
     *
     * Stores both the component info (with its renderer lambda) and
     * the lightweight [KComponentMetadata] for schema/documentation use.
     *
     * @param info The [KComponentInfo] describing the component, including
     *             its name, renderer lambda, and optional parameter types.
     * @see register
     */
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

    /**
     * Retrieve a registered component by name.
     *
     * Triggers lazy [initialize] if not yet called.
     *
     * @param name The component name (matching the JSON `"type"` key).
     * @return The [KComponentInfo], or `null` if not registered.
     */
    fun get(name: String): KComponentInfo? {
        if (!isInitialized) initialize()
        return components[name]
    }

    /**
     * Return all registered components as an immutable map.
     *
     * @return Map of component name → [KComponentInfo].
     */
    fun getAll(): Map<String, KComponentInfo> {
        if (!isInitialized) initialize()
        return components.toMap()
    }

    /**
     * Retrieve lightweight metadata for a registered component.
     *
     * @param name The component name.
     * @return The [KComponentMetadata], or `null` if not registered.
     */
    fun getMetadata(name: String): KComponentMetadata? = componentMetadata[name]

    /**
     * Return all registered component metadata as an immutable map.
     *
     * @return Map of component name → [KComponentMetadata].
     */
    fun getAllMetadata(): Map<String, KComponentMetadata> = componentMetadata.toMap()

    /**
     * Check whether a component with the given name is registered and
     * available for rendering.
     *
     * @param name The component name to check.
     * @return `true` if the component is registered.
     */
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

    /**
     * Clear all registered components and metadata, resetting the
     * initialisation flag.
     *
     * Primarily used in tests to restore a clean state.
     */
    fun clear() {
        components.clear()
        componentMetadata.clear()
        isInitialized = false
    }

    /**
     * Clear all registered data and immediately re-initialise the registry.
     *
     * Equivalent to calling [clear] followed by [initialize].
     */
    fun reset() {
        clear()
        initialize()
    }
}
