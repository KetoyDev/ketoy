package dev.ketoy.gradle.tasks

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for utility functions in the tasks package.
 */
class TaskUtilsTest {

    @Test
    fun `escapeJson escapes backslashes`() {
        assertEquals("\\\\", escapeJson("\\"))
    }

    @Test
    fun `escapeJson escapes double quotes`() {
        assertEquals("\\\"hello\\\"", escapeJson("\"hello\""))
    }

    @Test
    fun `escapeJson escapes newlines`() {
        assertEquals("line1\\nline2", escapeJson("line1\nline2"))
    }

    @Test
    fun `escapeJson escapes carriage returns`() {
        assertEquals("line1\\rline2", escapeJson("line1\rline2"))
    }

    @Test
    fun `escapeJson escapes tabs`() {
        assertEquals("col1\\tcol2", escapeJson("col1\tcol2"))
    }

    @Test
    fun `escapeJson handles complex JSON`() {
        val input = """{"key": "value", "nested": {"arr": [1, 2]}}"""
        val expected = """{\"key\": \"value\", \"nested\": {\"arr\": [1, 2]}}"""
        assertEquals(expected, escapeJson(input))
    }

    @Test
    fun `escapeJson handles empty string`() {
        assertEquals("", escapeJson(""))
    }

    @Test
    fun `missingConfig returns helpful error message`() {
        val msg = missingConfig("apiKey", "KETOY_DEVELOPER_API_KEY")
        assertTrue(msg.contains("apiKey"), "Message should mention the DSL property name")
        assertTrue(msg.contains("KETOY_DEVELOPER_API_KEY"), "Message should mention the property key")
        assertTrue(msg.contains("ketoyDev"), "Message should mention the extension block")
        assertTrue(msg.contains("local.properties"), "Message should mention local.properties")
    }
}
