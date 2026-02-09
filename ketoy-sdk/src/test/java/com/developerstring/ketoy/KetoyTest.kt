package com.developerstring.ketoy

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.model.KComponentInfo
import com.developerstring.ketoy.model.KetoyVariable
import com.developerstring.ketoy.registry.KComponentRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the [Ketoy] entry-point object.
 */
class KetoyTest {

    @Before
    fun setUp() {
        Ketoy.reset()
    }

    @After
    fun tearDown() {
        Ketoy.reset()
    }

    // ─── initialize ─────────────────────────────────────────────

    @Test
    fun `initialize sets initialized flag`() {
        assertFalse(Ketoy.isInitialized())
        Ketoy.initialize()
        assertTrue(Ketoy.isInitialized())
    }

    @Test
    fun `initialize is idempotent`() {
        Ketoy.initialize()
        Ketoy.initialize() // should not throw
        assertTrue(Ketoy.isInitialized())
    }

    @Test
    fun `initialize clears ActionRegistry`() {
        ActionRegistry.registerAction("test") { }
        assertNotNull(ActionRegistry.get("test"))
        Ketoy.initialize()
        assertNull(ActionRegistry.get("test"))
    }

    @Test
    fun `initialize clears VariableRegistry`() {
        KetoyVariableRegistry.register(KetoyVariable.Immutable("x", 1))
        Ketoy.initialize()
        assertNull(KetoyVariableRegistry.getValue("x"))
    }

    // ─── reset ──────────────────────────────────────────────────

    @Test
    fun `reset clears initialized state`() {
        Ketoy.initialize()
        assertTrue(Ketoy.isInitialized())
        Ketoy.reset()
        assertFalse(Ketoy.isInitialized())
    }

    @Test
    fun `reset clears all registries`() {
        Ketoy.initialize()
        ActionRegistry.registerAction("click") { }
        KetoyVariableRegistry.register(KetoyVariable.Mutable("v", "val"))
        KComponentRegistry.register(KComponentInfo(name = "W"))

        Ketoy.reset()

        assertNull(ActionRegistry.get("click"))
        assertNull(KetoyVariableRegistry.getValue("v"))
        assertNull(KComponentRegistry.get("W"))
    }

    @Test
    fun `can re-initialize after reset`() {
        Ketoy.initialize()
        Ketoy.reset()
        assertFalse(Ketoy.isInitialized())
        Ketoy.initialize()
        assertTrue(Ketoy.isInitialized())
    }
}
