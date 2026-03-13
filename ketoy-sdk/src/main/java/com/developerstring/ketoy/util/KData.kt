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
 * ### Predefined scopes
 * Common prefixes ship as named functions for convenience:
 * - [com.developerstring.ketoy.util.KData.user] — user/profile/ViewModel data
 * - [com.developerstring.ketoy.util.KData.analytics] — analytics screen data
 *
 * Custom prefixes use the generic [com.developerstring.ketoy.util.KData.ref] function.
 *
 * @see com.developerstring.ketoy.core.KetoyDataProvider
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

    /**
     * Builds a `{{data:user:field}}` template string.
     *
     * ```kotlin
     * KData.user("name")          // → "{{data:user:name}}"
     * KData.user("totalBalance")  // → "{{data:user:totalBalance}}"
     * ```
     */
    fun user(field: String): String = ref("user", field)

    /**
     * Builds a `{{data:analytics:field}}` template string.
     *
     * ```kotlin
     * KData.analytics("income")  // → "{{data:analytics:income}}"
     * ```
     */
    fun analytics(field: String): String = ref("analytics", field)
}
