package com.developerstring.ketoy.core

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ActionRegistry].
 */
class ActionRegistryTest {

    @Before
    fun setUp() {
        ActionRegistry.clear()
    }

    @After
    fun tearDown() {
        ActionRegistry.clear()
    }

    // ─── register (auto-ID) ─────────────────────────────────────

    @Test
    fun `register returns unique auto-generated ID`() {
        val id1 = ActionRegistry.register { }
        val id2 = ActionRegistry.register { }
        assertNotEquals(id1, id2)
        assertTrue(id1.startsWith("action_"))
        assertTrue(id2.startsWith("action_"))
    }

    @Test
    fun `registered action is retrievable`() {
        val id = ActionRegistry.register { }
        assertNotNull(ActionRegistry.get(id))
    }

    @Test
    fun `unregistered ID returns null`() {
        assertNull(ActionRegistry.get("nonexistent"))
    }

    // ─── registerAction (named ID) ──────────────────────────────

    @Test
    fun `registerAction with explicit ID stores action`() {
        ActionRegistry.registerAction("my_action") { }
        assertNotNull(ActionRegistry.get("my_action"))
    }

    @Test
    fun `registerAction overwrites existing action`() {
        var callCount = 0
        ActionRegistry.registerAction("a") { callCount = 1 }
        ActionRegistry.registerAction("a") { callCount = 2 }
        ActionRegistry.execute("a")
        assertEquals(2, callCount)
    }

    // ─── execute ────────────────────────────────────────────────

    @Test
    fun `execute invokes registered action`() {
        var invoked = false
        val id = ActionRegistry.register { invoked = true }
        ActionRegistry.execute(id)
        assertTrue(invoked)
    }

    @Test
    fun `execute with unknown ID does nothing`() {
        ActionRegistry.execute("unknown") // should not throw
    }

    // ─── registerTextChange ─────────────────────────────────────

    @Test
    fun `registerTextChange returns unique ID`() {
        val id = ActionRegistry.registerTextChange { }
        assertTrue(id.startsWith("textChange_"))
    }

    @Test
    fun `registerTextChange with explicit ID`() {
        ActionRegistry.registerTextChange("email_change") { }
        assertNotNull(ActionRegistry.getTextChange("email_change"))
    }

    @Test
    fun `executeTextChange passes value to callback`() {
        var received = ""
        val id = ActionRegistry.registerTextChange { received = it }
        ActionRegistry.executeTextChange(id, "hello")
        assertEquals("hello", received)
    }

    @Test
    fun `getTextChange for unknown ID returns null`() {
        assertNull(ActionRegistry.getTextChange("nope"))
    }

    // ─── clear ──────────────────────────────────────────────────

    @Test
    fun `clear removes all actions`() {
        val id1 = ActionRegistry.register { }
        val id2 = ActionRegistry.registerTextChange { }
        ActionRegistry.clear()
        assertNull(ActionRegistry.get(id1))
        assertNull(ActionRegistry.getTextChange(id2))
    }

    @Test
    fun `clear resets counter so IDs restart`() {
        ActionRegistry.register { }
        ActionRegistry.register { }
        ActionRegistry.clear()
        val id = ActionRegistry.register { }
        assertEquals("action_0", id)
    }

    // ─── Concurrency safety (basic) ─────────────────────────────

    @Test
    fun `multiple registrations maintain separate state`() {
        var a = 0; var b = 0
        val id1 = ActionRegistry.register { a++ }
        val id2 = ActionRegistry.register { b++ }
        ActionRegistry.execute(id1)
        ActionRegistry.execute(id1)
        ActionRegistry.execute(id2)
        assertEquals(2, a)
        assertEquals(1, b)
    }
}
