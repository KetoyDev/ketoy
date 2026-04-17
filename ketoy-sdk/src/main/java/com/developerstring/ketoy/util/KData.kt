/**
 * Type-safe data-template builder for the Ketoy SDUI variable system.
 *
 * Instead of writing raw template strings like `"{{data:user:name}}"`,
 * use `KData` to get compile-time safety and IDE auto-completion:
 *
 * ```kotlin
 * // Type-safe (recommended):
 * KText(text = KData.user("name"))         // → "{{data:user:name}}"
 * KText(text = KData.ref("analytics", "income"))  // → "{{data:analytics:income}}"
 *
 * // Raw string (error-prone, avoid):
 * KText(text = "{{data:user:name}}")
 * ```
 *
 * ### All methods return `String`
 * Templates are always serialised as strings in JSON / wire format. The renderer
 * resolves them to the correct runtime type. For DSL builder parameters that accept
 * non-String values (e.g. `Int`, `Boolean`), declare the parameter as `Any` and
 * resolve it at use-time with the typed helpers in
 * [KetoyVariableRegistry][com.developerstring.ketoy.core.KetoyVariableRegistry]:
 *
 * ```kotlin
 * // DSL builder:
 * fun buildScreen(selectedIndex: Any = 0): KNode = ketoyRoot {
 *     val index = KetoyVariableRegistry.resolveInt(selectedIndex)
 *     // … use index in logic …
 * }
 *
 * // Export (template):
 * buildScreen(selectedIndex = KData.userInt("selectedIndex"))
 *
 * // Runtime (literal):
 * buildScreen(selectedIndex = viewModel.selectedIndex)
 * ```
 *
 * ### Predefined scopes
 * Common prefixes ship as named functions for convenience:
 * - [com.developerstring.ketoy.util.KData.user] — user/profile/ViewModel data
 * - [com.developerstring.ketoy.util.KData.analytics] — analytics screen data
 *
 * Typed variants (`userInt`, `userBool`, etc.) are identical at runtime but
 * document the expected resolved type for the reader.
 *
 * Custom prefixes use the generic [com.developerstring.ketoy.util.KData.ref] function.
 *
 * @see com.developerstring.ketoy.core.KetoyVariableRegistry
 */
package com.developerstring.ketoy.util

object KData {

    /**
     * Builds a `{{data:prefix:field}}` template string.
     *
     * ```kotlin
     * KData.ref("user", "name")  // → "{{data:user:name}}"
     * ```
     */
    fun ref(prefix: String, field: String): String = "{{data:$prefix:$field}}"

    // ── String templates ─────────────────────────────────────

    /**
     * Builds a `{{data:user:field}}` template string (resolved as [String]).
     *
     * ```kotlin
     * KData.user("name")          // → "{{data:user:name}}"
     * KData.user("totalBalance")  // → "{{data:user:totalBalance}}"
     * ```
     */
    fun user(field: String): String = ref("user", field)

    /**
     * Builds a `{{data:analytics:field}}` template string (resolved as [String]).
     *
     * ```kotlin
     * KData.analytics("income")  // → "{{data:analytics:income}}"
     * ```
     */
    fun analytics(field: String): String = ref("analytics", field)

    // ── Typed template helpers ───────────────────────────────
    // These are identical to their untyped equivalents at runtime — they all
    // return a String template. The `Int`/`Bool`/`Float` suffix documents the
    // expected resolved type and pairs with KetoyVariableRegistry.resolveXxx().

    /**
     * Template for an [Int] field in the `user` scope.
     * Pass to an `Any`-typed DSL parameter and resolve with
     * `KetoyVariableRegistry.resolveInt(value)`.
     *
     * ```kotlin
     * buildScreen(count = KData.userInt("notificationCount"))
     * ```
     */
    fun userInt(field: String): String = user(field)

    /**
     * Template for a [Boolean] field in the `user` scope.
     * Resolve with `KetoyVariableRegistry.resolveBoolean(value)`.
     */
    fun userBool(field: String): String = user(field)

    /**
     * Template for a [Float] field in the `user` scope.
     * Resolve with `KetoyVariableRegistry.resolveFloat(value)`.
     */
    fun userFloat(field: String): String = user(field)

    /**
     * Template for a [Double] field in the `user` scope.
     * Resolve with `KetoyVariableRegistry.resolveDouble(value)`.
     */
    fun userDouble(field: String): String = user(field)

    /**
     * Template for a [Long] field in the `user` scope.
     * Resolve with `KetoyVariableRegistry.resolveLong(value)`.
     */
    fun userLong(field: String): String = user(field)

    /**
     * Template for an [Int] field in the `analytics` scope.
     * Resolve with `KetoyVariableRegistry.resolveInt(value)`.
     */
    fun analyticsInt(field: String): String = analytics(field)

    /**
     * Template for a [Boolean] field in the `analytics` scope.
     * Resolve with `KetoyVariableRegistry.resolveBoolean(value)`.
     */
    fun analyticsBool(field: String): String = analytics(field)

    /**
     * Template for a [Float] field in the `analytics` scope.
     * Resolve with `KetoyVariableRegistry.resolveFloat(value)`.
     */
    fun analyticsFloat(field: String): String = analytics(field)

    /**
     * Template for a [Double] field in the `analytics` scope.
     * Resolve with `KetoyVariableRegistry.resolveDouble(value)`.
     */
    fun analyticsDouble(field: String): String = analytics(field)

    /**
     * Template for a generic [Int] field.
     * Equivalent to `KData.ref(prefix, field)`.
     * Resolve with `KetoyVariableRegistry.resolveInt(value)`.
     */
    fun intRef(prefix: String, field: String): String = ref(prefix, field)

    /**
     * Template for a generic [Boolean] field.
     * Resolve with `KetoyVariableRegistry.resolveBoolean(value)`.
     */
    fun boolRef(prefix: String, field: String): String = ref(prefix, field)

    /**
     * Template for a generic [Float] field.
     * Resolve with `KetoyVariableRegistry.resolveFloat(value)`.
     */
    fun floatRef(prefix: String, field: String): String = ref(prefix, field)

    /**
     * Template for a generic [Double] field.
     * Resolve with `KetoyVariableRegistry.resolveDouble(value)`.
     */
    fun doubleRef(prefix: String, field: String): String = ref(prefix, field)

    /**
     * Template for a generic [Long] field.
     * Resolve with `KetoyVariableRegistry.resolveLong(value)`.
     */
    fun longRef(prefix: String, field: String): String = ref(prefix, field)
}
