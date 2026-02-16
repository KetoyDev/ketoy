package com.developerstring.ketoy.devserver

/**
 * Generates a simple ASCII QR code in the terminal.
 * Uses a lightweight QR code encoding algorithm (no external deps).
 * 
 * This is a simplified version that generates a URL-style display
 * since full QR code generation from scratch is complex.
 * For production, use a QR library like zxing.
 */
object QRCodeGenerator {

    fun printQR(url: String) {
        // Generate a text-based "scannable" box with the URL
        // For a real QR code, integrate com.google.zxing
        val maxLen = maxOf(url.length + 4, 40)
        val border = "█".repeat(maxLen)
        val inner = "█" + " ".repeat(maxLen - 2) + "█"

        println("   ┌${"─".repeat(maxLen)}┐")
        println("   │$border│")
        println("   │$inner│")

        val padded = url.padEnd(maxLen - 4)
        println("   │█ $padded █│")

        println("   │$inner│")
        println("   │$border│")
        println("   └${"─".repeat(maxLen)}┘")
        println()
        println("   💡 Tip: Install 'qrencode' (brew install qrencode) for a real QR code:")
        println("      echo '$url' | qrencode -t ANSIUTF8")
    }
}
