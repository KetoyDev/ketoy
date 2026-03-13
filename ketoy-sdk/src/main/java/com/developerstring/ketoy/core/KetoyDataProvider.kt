package com.developerstring.ketoy.core

import com.developerstring.ketoy.model.KetoyVariable

/**
 * Interface for exposing structured data to the Ketoy SDUI variable system.
 *
 * Any class — ViewModel, data class, sealed class instance, or custom object —
 * can implement this interface to automatically register its fields as
 * `{{data:prefix:field}}` template variables in [KetoyVariableRegistry].
 *
 * ## Example — ViewModel
 * ```kotlin
 * class UserViewModel : ViewModel(), KetoyDataProvider {
 *     override val ketoyPrefix = "user"
 *     var name by mutableStateOf("Alice")
 *     var balance by mutableStateOf(1234.56)
 *
 *     override fun provideData(): Map<String, Any?> = mapOf(
 *         "name" to name,
 *         "balance" to "$${"%,.2f".format(balance)}",
 *         "initials" to name.take(2).uppercase()
 *     )
 * }
 * ```
 *
 * Then in your Activity/Fragment:
 * ```kotlin
 * userViewModel.syncToKetoy() // registers user.name, user.balance, user.initials
 * ```
 *
 * ## Example — Data class
 * ```kotlin
 * data class Transaction(
 *     val title: String,
 *     val amount: Double,
 *     val isIncome: Boolean
 * ) : KetoyDataProvider {
 *     override val ketoyPrefix = "transaction"
 *     override fun provideData() = mapOf(
 *         "title" to title,
 *         "amount" to amount,
 *         "isIncome" to isIncome
 *     )
 * }
 * ```
 *
 * ## Example — Sealed class
 * ```kotlin
 * sealed class PaymentMethod : KetoyDataProvider {
 *     override val ketoyPrefix get() = "payment"
 *
 *     data class Card(val last4: String, val brand: String) : PaymentMethod() {
 *         override fun provideData() = mapOf(
 *             "type" to "card", "last4" to last4, "brand" to brand
 *         )
 *     }
 *     data class Bank(val bankName: String) : PaymentMethod() {
 *         override fun provideData() = mapOf(
 *             "type" to "bank", "bankName" to bankName
 *         )
 *     }
 * }
 * ```
 *
 * @see syncToKetoy
 * @see syncListToKetoy
 * @see KetoyVariableRegistry
 */
interface KetoyDataProvider {

    /**
     * Prefix used for variable registration.
     * A field named `"name"` with prefix `"user"` becomes variable `"user.name"`,
     * resolvable via template `{{data:user:name}}`.
     */
    val ketoyPrefix: String

    /**
     * Returns the key-value pairs to register as Ketoy variables.
     * Keys become the field part of `{{data:prefix:field}}` templates.
     * Values can be any type — they are stored via [KetoyVariable.Mutable].
     */
    fun provideData(): Map<String, Any?>
}

/**
 * Syncs all fields from this [KetoyDataProvider] into [KetoyVariableRegistry]
 * as mutable variables. Call this whenever the backing data changes.
 *
 * Each entry from [KetoyDataProvider.provideData] is registered as
 * `"$prefix.$field"` → value, making it available as `{{data:prefix:field}}`.
 */
fun KetoyDataProvider.syncToKetoy() {
    val prefix = ketoyPrefix
    provideData().forEach { (field, value) ->
        KetoyVariableRegistry.register(
            KetoyVariable.Mutable("$prefix.$field", value ?: "")
        )
    }
}

/**
 * Syncs a list of [KetoyDataProvider] items, registering each item's fields
 * with an indexed prefix: `"$basePrefix.$index.$field"`.
 *
 * This makes list items addressable as `{{data:transactions:0:title}}`, etc.
 * Also registers a `$basePrefix.count` variable with the list size.
 *
 * @param basePrefix The shared prefix for all items (e.g., `"transactions"`).
 * @param items The list of providers to register.
 */
fun <T : KetoyDataProvider> syncListToKetoy(basePrefix: String, items: List<T>) {
    KetoyVariableRegistry.register(
        KetoyVariable.Mutable("$basePrefix.count", items.size)
    )
    items.forEachIndexed { index, item ->
        item.provideData().forEach { (field, value) ->
            KetoyVariableRegistry.register(
                KetoyVariable.Mutable("$basePrefix.$index.$field", value ?: "")
            )
        }
    }
}

/**
 * Generates a map of template strings for this provider's fields.
 * Useful in DSL builders during export to produce `{{data:prefix:field}}`
 * placeholders instead of actual values.
 *
 * ```kotlin
 * val templates = userProvider.toTemplateMap()
 * // { "name" -> "{{data:user:name}}", "balance" -> "{{data:user:balance}}" }
 * ```
 */
fun KetoyDataProvider.toTemplateMap(): Map<String, String> {
    val prefix = ketoyPrefix
    return provideData().keys.associateWith { field -> "{{data:$prefix:$field}}" }
}
