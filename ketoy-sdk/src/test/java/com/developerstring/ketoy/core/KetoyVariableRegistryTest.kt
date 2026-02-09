package com.developerstring.ketoy.core

import com.developerstring.ketoy.model.KetoyVariable
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [KetoyVariableRegistry] and DSL convenience functions.
 */
class KetoyVariableRegistryTest {

    @Before
    fun setUp() {
        KetoyVariableRegistry.clear()
    }

    @After
    fun tearDown() {
        KetoyVariableRegistry.clear()
    }

    // ─── Register & Retrieve ────────────────────────────────────

    @Test
    fun `register immutable variable and retrieve value`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("name", "Alice"))
        assertEquals("Alice", KetoyVariableRegistry.getValue("name"))
    }

    @Test
    fun `register mutable variable and retrieve value`() {
        KetoyVariableRegistry.register(KetoyVariable.Mutable("count", 42))
        assertEquals(42, KetoyVariableRegistry.getValue("count"))
    }

    @Test
    fun `get returns typed variable`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("pi", 3.14))
        val v = KetoyVariableRegistry.get<Double>("pi")
        assertNotNull(v)
        assertEquals(3.14, v!!.value as Double, 0.001)
    }

    @Test
    fun `get returns null for unknown ID`() {
        assertNull(KetoyVariableRegistry.get<String>("unknown"))
    }

    @Test
    fun `getValue returns null for unknown ID`() {
        assertNull(KetoyVariableRegistry.getValue("unknown"))
    }

    // ─── Update ─────────────────────────────────────────────────

    @Test
    fun `updateValue on mutable variable returns true`() {
        KetoyVariableRegistry.register(KetoyVariable.Mutable("score", 0))
        val updated = KetoyVariableRegistry.updateValue("score", 100)
        assertTrue(updated)
        assertEquals(100, KetoyVariableRegistry.getValue("score"))
    }

    @Test
    fun `updateValue on immutable variable returns false`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("name", "Alice"))
        val updated = KetoyVariableRegistry.updateValue("name", "Bob")
        assertFalse(updated)
        assertEquals("Alice", KetoyVariableRegistry.getValue("name"))
    }

    @Test
    fun `updateValue on unknown ID returns false`() {
        assertFalse(KetoyVariableRegistry.updateValue("nope", 1))
    }

    // ─── getAllVariables ─────────────────────────────────────────

    @Test
    fun `getAllVariables returns snapshot`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("a", 1))
        KetoyVariableRegistry.register(KetoyVariable.Mutable("b", 2))
        val all = KetoyVariableRegistry.getAllVariables()
        assertEquals(2, all.size)
        assertTrue(all.containsKey("a"))
        assertTrue(all.containsKey("b"))
    }

    // ─── Clear ──────────────────────────────────────────────────

    @Test
    fun `clear removes all variables`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("x", 1))
        KetoyVariableRegistry.clear()
        assertNull(KetoyVariableRegistry.getValue("x"))
        assertTrue(KetoyVariableRegistry.getAllVariables().isEmpty())
    }

    // ─── Template resolution ────────────────────────────────────

    @Test
    fun `resolveTemplate replaces data placeholders`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("user.name", "Alice"))
        val result = KetoyVariableRegistry.resolveTemplate("Hello {{data:user:name}}!")
        assertEquals("Hello Alice!", result)
    }

    @Test
    fun `resolveTemplate replaces enum placeholders`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("status.selectedValue", "Active"))
        val result = KetoyVariableRegistry.resolveTemplate("Status: {{enum:status:selectedValue}}")
        assertEquals("Status: Active", result)
    }

    @Test
    fun `resolveTemplate missing key shows placeholder`() {
        val result = KetoyVariableRegistry.resolveTemplate("Hi {{data:unknown:field}}")
        assertTrue(result.contains("[Missing:"))
    }

    @Test
    fun `resolveTemplate with no placeholders returns original`() {
        val input = "No placeholders here"
        assertEquals(input, KetoyVariableRegistry.resolveTemplate(input))
    }

    @Test
    fun `resolveTemplate handles multiple placeholders`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("user.first", "John"))
        KetoyVariableRegistry.register(KetoyVariable.Immutable("user.last", "Doe"))
        val result = KetoyVariableRegistry.resolveTemplate("{{data:user:first}} {{data:user:last}}")
        assertEquals("John Doe", result)
    }

    // ─── DSL convenience functions ──────────────────────────────

    @Test
    fun `ketoyRemember registers immutable variable`() {
        val v = ketoyRemember("greeting", "Hi")
        assertEquals("Hi", v.value)
        assertEquals("greeting", v.id)
        assertEquals("Hi", KetoyVariableRegistry.getValue("greeting"))
    }

    @Test
    fun `ketoyMutableStateOf registers mutable variable`() {
        val v = ketoyMutableStateOf("counter", 0)
        assertEquals(0, v.value)
        assertEquals("counter", v.id)
        KetoyVariableRegistry.updateValue("counter", 5)
        assertEquals(5, KetoyVariableRegistry.getValue("counter"))
    }

    @Test
    fun `variableValue resolves template`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("app.title", "Ketoy"))
        val result = variableValue("Welcome to {{data:app:title}}")
        assertEquals("Welcome to Ketoy", result)
    }
}
