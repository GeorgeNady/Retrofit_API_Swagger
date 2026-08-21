package com.github.georgenady.rettrofitapigraph.presentation.theme

import com.intellij.ui.JBColor
import java.awt.Color

object SwaggerTheme {

    // Light and Dark Mode Color Definitions
    val GET = HttpMethodTheme(
        backgroundColor = JBColor(Color(0xEB, 0xF3, 0xFB), Color(0x1B, 0x2A, 0x3A)),
        borderColor = JBColor(Color(0x61, 0xAF, 0xFE), Color(0x2B, 0x6C, 0xB0)),
        badgeColor = Color(0x61, 0xAF, 0xFE)
    )

    val POST = HttpMethodTheme(
        backgroundColor = JBColor(Color(0xE8, 0xF6, 0xF0), Color(0x19, 0x33, 0x28)),
        borderColor = JBColor(Color(0x49, 0xCC, 0x90), Color(0x23, 0x86, 0x59)),
        badgeColor = Color(0x49, 0xCC, 0x90)
    )

    val PUT = HttpMethodTheme(
        backgroundColor = JBColor(Color(0xFB, 0xF1, 0xE6), Color(0x3B, 0x2E, 0x1E)),
        borderColor = JBColor(Color(0xFC, 0xA1, 0x30), Color(0x8C, 0x53, 0x11)),
        badgeColor = Color(0xFC, 0xA1, 0x30)
    )

    val DELETE = HttpMethodTheme(
        backgroundColor = JBColor(Color(0xFA, 0xE7, 0xE7), Color(0x3B, 0x1E, 0x1E)),
        borderColor = JBColor(Color(0xF9, 0x3E, 0x3E), Color(0xA3, 0x21, 0x21)),
        badgeColor = Color(0xF9, 0x3E, 0x3E)
    )

    val PATCH = HttpMethodTheme(
        backgroundColor = JBColor(Color(0xF4, 0xF9, 0xF7), Color(0x1E, 0x3B, 0x33)),
        borderColor = JBColor(Color(0x50, 0xE3, 0xC2), Color(0x21, 0xA3, 0x87)),
        badgeColor = Color(0x50, 0xE3, 0xC2)
    )

    fun getThemeForMethod(method: String): HttpMethodTheme {
        return when (method.uppercase()) {
            "GET" -> GET
            "POST" -> POST
            "PUT" -> PUT
            "DELETE" -> DELETE
            "PATCH" -> PATCH
            else -> GET
        }
    }
}
