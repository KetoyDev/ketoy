package com.developerstring.ketoy.registry

/**
 * Global registry for Kotlin functions callable from Ketoy SDUI JSON.
 *
 * Developers register named functions with typed parameters, then
 * reference them from JSON actions. This bridges the gap between
 * server-driven UI and app-side business logic.
 *
 * ## Registering functions
 *
 * ```kotlin
 * // Simple function (no params)
 * KetoyFunctionRegistry.register("logout") {
 *     authManager.signOut()
 * }
 *
 * // Function with parameters
 * KetoyFunctionRegistry.register(
 *     name = "addToCart",
 *     parameterTypes = mapOf("productId" to "String", "quantity" to "Int")
 * ) { params ->
 *     val productId = params["productId"] as? String ?: return@register
 *     val quantity = params["quantity"] as? Int ?: 1
 *     cartRepository.add(productId, quantity)
 * }
 *
 * // Function with return value (returned via callback)
 * KetoyFunctionRegistry.register(
 *     name = "getPrice",
 *     parameterTypes = mapOf("productId" to "String")
 * ) { params ->
 *     val productId = params["productId"] as? String ?: return@register
 *     val price = productRepository.getPrice(productId)
 *     // Return value is available for chaining in multiAction
 * }
 * ```
 *
 * ## Calling from JSON
 *
 * ```json
 * {
 *     "onClick": {
 *         "actionType": "callFunction",
 *         "functionName": "addToCart",
 *         "arguments": {
 *             "productId": "SKU-12345",
 *             "quantity": 2
 *         }
 *     }
 * }
 * ```
 *
 * ## Using in DSL
 *
 * ```kotlin
 * KButton(onClick = { }, actionId = "btn") {
 *     KText("Add to Cart")
 * }
 * // Or use the KFunctionCall helper:
 * KFunctionCall(
 *     functionName = "addToCart",
 *     arguments = mapOf("productId" to "SKU-123", "quantity" to 1)
 * )
 * ```
 */
object KetoyFunctionRegistry {

    private const val TAG = "KetoyFunctionRegistry"

    /**
     * Describes a registered function.
     *
     * @property name           Function identifier used in JSON `"functionName"`.
     * @property handler        The function body receiving typed parameters.
     * @property parameterTypes Map of param name → type name (for docs / schema).
     * @property description    Optional human-readable description.
     */
    data class FunctionInfo(
        val name: String,
        val handler: (Map<String, Any>) -> Unit,
        val parameterTypes: Map<String, String> = emptyMap(),
        val description: String = ""
    )

    private val functions = mutableMapOf<String, FunctionInfo>()

    // ── Registration ────────────────────────────────────────────

    /**
     * Register a function with typed parameters.
     *
     * @param name           Unique function name referenced from JSON.
     * @param parameterTypes Map of param name → type (e.g. `"productId" to "String"`).
     *                       **Required** — pass `emptyMap()` if no params.
     * @param description    Optional description for documentation.
     * @param handler        The function body. Receives a `Map<String, Any>` of
     *                       typed arguments extracted from JSON primitives.
     */
    fun register(
        name: String,
        parameterTypes: Map<String, String>,
        description: String = "",
        handler: (params: Map<String, Any>) -> Unit
    ) {
        functions[name] = FunctionInfo(
            name = name,
            handler = handler,
            parameterTypes = parameterTypes,
            description = description
        )
        println("$TAG: Registered function '$name' with params: ${parameterTypes.keys}")
    }

    /**
     * Register a simple no-argument function.
     *
     * ```kotlin
     * KetoyFunctionRegistry.register("logout") {
     *     authManager.signOut()
     * }
     * ```
     *
     * @param name        Unique function name referenced from JSON `"functionName"`.
     * @param description Optional human-readable description.
     * @param handler     The function body (receives no arguments).
     */
    fun register(name: String, description: String = "", handler: () -> Unit) {
        register(
            name = name,
            parameterTypes = emptyMap(),
            description = description
        ) { _ -> handler() }
    }

    // ── Execution ───────────────────────────────────────────────

    /**
     * Call a registered function by name with arguments.
     *
     * @param name      The function name.
     * @param arguments Typed argument map.
     * @return `true` if function was found and executed, `false` otherwise.
     */
    fun call(name: String, arguments: Map<String, Any> = emptyMap()): Boolean {
        val func = functions[name]
        if (func == null) {
            println("$TAG: WARNING - Function '$name' not registered")
            return false
        }
        try {
            func.handler(arguments)
            return true
        } catch (e: Exception) {
            println("$TAG: ERROR executing function '$name': ${e.message}")
            return false
        }
    }

    // ── Query ───────────────────────────────────────────────────

    /** Get a function’s info by name.
     *
     * @param name The function name to look up.
     * @return The [FunctionInfo], or `null` if not registered.
     */
    fun get(name: String): FunctionInfo? = functions[name]

    /**
     * Check if a function is registered.
     *
     * @param name The function name to check.
     * @return `true` if the function is registered.
     */
    fun isRegistered(name: String): Boolean = functions.containsKey(name)

    /**
     * Get all registered function names.
     *
     * @return An immutable [Set] of function name strings.
     */
    fun getAllNames(): Set<String> = functions.keys.toSet()

    /**
     * Get all registered functions with their metadata.
     *
     * @return An immutable map of function name → [FunctionInfo].
     */
    fun getAll(): Map<String, FunctionInfo> = functions.toMap()

    // ── Lifecycle ───────────────────────────────────────────────

    /**
     * Remove a function by name.
     *
     * @param name The function name to unregister.
     * @return `true` if the function was removed, `false` if it was not found.
     */
    fun remove(name: String): Boolean = functions.remove(name) != null

    /**
     * Clear all registered functions.
     *
     * Typically used in tests to reset state between test cases.
     */
    fun clear() {
        functions.clear()
    }
}
