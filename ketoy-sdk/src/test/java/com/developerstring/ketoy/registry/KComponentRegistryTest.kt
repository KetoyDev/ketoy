package com.developerstring.ketoy.registry

import com.developerstring.ketoy.model.KComponentInfo
import com.developerstring.ketoy.model.KComponentMetadata
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [KComponentRegistry].
 */
class KComponentRegistryTest {

    @Before
    fun setUp() {
        KComponentRegistry.reset()
    }

    @After
    fun tearDown() {
        KComponentRegistry.clear()
    }

    // ─── Initialize ─────────────────────────────────────────────

    @Test
    fun `initialize is idempotent`() {
        KComponentRegistry.initialize()
        KComponentRegistry.initialize() // should not throw
    }

    // ─── Register info ──────────────────────────────────────────

    @Test
    fun `register and retrieve by name`() {
        val info = KComponentInfo(name = "CustomCard", description = "A custom card")
        KComponentRegistry.register(info)
        val retrieved = KComponentRegistry.get("CustomCard")
        assertNotNull(retrieved)
        assertEquals("CustomCard", retrieved!!.name)
        assertEquals("A custom card", retrieved.description)
    }

    @Test
    fun `register with parameterTypes via KComponentInfo`() {
        KComponentRegistry.register(
            KComponentInfo(
                name = "Widget",
                parameterTypes = mapOf("title" to "String", "count" to "Int")
            )
        )
        val info = KComponentRegistry.get("Widget")
        assertNotNull(info)
        assertEquals("String", info!!.parameterTypes["title"])
        assertEquals("Int", info.parameterTypes["count"])
    }

    @Test
    fun `get returns null for unregistered component`() {
        assertNull(KComponentRegistry.get("Unknown"))
    }

    // ─── getAll ─────────────────────────────────────────────────

    @Test
    fun `getAll returns all registered components`() {
        KComponentRegistry.register(KComponentInfo(name = "A"))
        KComponentRegistry.register(KComponentInfo(name = "B"))
        val all = KComponentRegistry.getAll()
        assertEquals(2, all.size)
        assertTrue(all.containsKey("A"))
        assertTrue(all.containsKey("B"))
    }

    // ─── Metadata ───────────────────────────────────────────────

    @Test
    fun `register auto-creates metadata`() {
        KComponentRegistry.register(KComponentInfo(
            name = "MyWidget", packageName = "com.example", version = "2.0"
        ))
        val meta = KComponentRegistry.getMetadata("MyWidget")
        assertNotNull(meta)
        assertEquals("MyWidget", meta!!.name)
        assertEquals("com.example", meta.packageName)
        assertEquals("2.0", meta.version)
    }

    @Test
    fun `getAllMetadata returns metadata for all components`() {
        KComponentRegistry.register(KComponentInfo(name = "X"))
        KComponentRegistry.register(KComponentInfo(name = "Y"))
        assertEquals(2, KComponentRegistry.getAllMetadata().size)
    }

    @Test
    fun `getMetadata returns null for unknown`() {
        assertNull(KComponentRegistry.getMetadata("Nope"))
    }

    // ─── isAvailable ────────────────────────────────────────────

    @Test
    fun `isAvailable returns true for registered component`() {
        KComponentRegistry.register(KComponentInfo(name = "Present"))
        assertTrue(KComponentRegistry.isAvailable("Present"))
    }

    @Test
    fun `isAvailable returns false for missing component`() {
        assertFalse(KComponentRegistry.isAvailable("Missing"))
    }

    // ─── loadFromMetadata ───────────────────────────────────────

    @Test
    fun `loadFromMetadata returns true if already registered`() {
        KComponentRegistry.register(KComponentInfo(name = "Existing"))
        assertTrue(KComponentRegistry.loadFromMetadata(KComponentMetadata(name = "Existing")))
    }

    @Test
    fun `loadFromMetadata returns false for truly missing component`() {
        assertFalse(KComponentRegistry.loadFromMetadata(KComponentMetadata(name = "MissingLib")))
    }

    // ─── clear / reset ──────────────────────────────────────────

    @Test
    fun `clear removes all components`() {
        KComponentRegistry.register(KComponentInfo(name = "Temp"))
        KComponentRegistry.clear()
        assertNull(KComponentRegistry.get("Temp"))
        assertTrue(KComponentRegistry.getAll().isEmpty())
    }

    @Test
    fun `reset clears and re-initializes`() {
        KComponentRegistry.register(KComponentInfo(name = "Old"))
        KComponentRegistry.reset()
        assertNull(KComponentRegistry.get("Old"))
        // Should still be usable
        KComponentRegistry.register(KComponentInfo(name = "New"))
        assertNotNull(KComponentRegistry.get("New"))
    }

    // ─── Overwrite ──────────────────────────────────────────────

    @Test
    fun `registering same name overwrites previous`() {
        KComponentRegistry.register(KComponentInfo(name = "Widget", version = "1.0"))
        KComponentRegistry.register(KComponentInfo(name = "Widget", version = "2.0"))
        val info = KComponentRegistry.get("Widget")
        assertEquals("2.0", info!!.version)
    }
}
