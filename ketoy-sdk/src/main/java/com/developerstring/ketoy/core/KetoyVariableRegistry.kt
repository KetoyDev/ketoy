package com.developerstring.ketoy.core

import com.developerstring.ketoy.model.KetoyVariable
import kotlinx.serialization.Serializable

/**
 * Simple state-management registry for Ketoy variables.
 *
 * Variables are stored by a string [id] and can be immutable or mutable.
 * Templates such as `{{data:userId:name}}` are resolved at render time.
 */
object KetoyVariableRegistry {

    private val variables = mutableMapOf<String, KetoyVariable<*>>()

    /* ── CRUD ────────────────────────────────────────── */

    fun <T> register(variable: KetoyVariable<T>): KetoyVariable<T> {
        variables[variable.id] = variable
        return variable
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(id: String): KetoyVariable<T>? = variables[id] as? KetoyVariable<T>

    fun getValue(id: String): Any? = variables[id]?.value

    fun <T> updateValue(id: String, newValue: T): Boolean {
        val variable = variables[id]
        return when (variable) {
            is KetoyVariable.Mutable<*> -> {
                @Suppress("UNCHECKED_CAST")
                variables[id] = (variable as KetoyVariable.Mutable<T>).copy(value = newValue)
                true
            }
            else -> false
        }
    }

    fun clear() {
        variables.clear()
    }

    fun getAllVariables(): Map<String, KetoyVariable<*>> = variables.toMap()

    /* ── Template resolution ─────────────────────────── */

    /**
     * Resolve `{{data:id:field}}` and `{{enum:id:property}}` placeholders
     * within the supplied [template] string.
     */
    fun resolveTemplate(template: String): String {
        val dataRegex = "\\{\\{data:([^:]+):([^}]+)\\}\\}".toRegex()
        val dataResolved = dataRegex.replace(template) { match ->
            val key = "${match.groupValues[1]}.${match.groupValues[2]}"
            getValue(key)?.toString() ?: "[Missing: $key]"
        }

        val enumRegex = "\\{\\{enum:([^:]+):([^}]+)\\}\\}".toRegex()
        return enumRegex.replace(dataResolved) { match ->
            val key = "${match.groupValues[1]}.${match.groupValues[2]}"
            getValue(key)?.toString() ?: "[Missing: $key]"
        }
    }
}

/* ── DSL convenience functions ──────────────────────── */

fun <T> ketoyRemember(id: String, value: T): KetoyVariable.Immutable<T> {
    @Suppress("UNCHECKED_CAST")
    return KetoyVariableRegistry.register(KetoyVariable.Immutable(id, value)) as KetoyVariable.Immutable<T>
}

fun <T> ketoyMutableStateOf(id: String, value: T): KetoyVariable.Mutable<T> {
    @Suppress("UNCHECKED_CAST")
    return KetoyVariableRegistry.register(KetoyVariable.Mutable(id, value)) as KetoyVariable.Mutable<T>
}

fun variableValue(template: String): String =
    KetoyVariableRegistry.resolveTemplate(template)
