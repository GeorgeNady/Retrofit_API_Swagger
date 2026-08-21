package com.github.georgenady.rettrofitapigraph.presentation.components

import com.intellij.ui.components.JBLabel
import java.awt.*
import javax.swing.SwingConstants

class BadgeLabel(
    text: String,
    private val badgeColor: Color
) : JBLabel(text, CENTER) {

    init {
        foreground = Color.WHITE
        font = font.deriveFont(Font.BOLD, 12f)
        preferredSize = Dimension(70, 26)
        isOpaque = false
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = badgeColor
            g2.fillRoundRect(0, 0, width, height, 6, 6)
        } finally {
            g2.dispose()
        }
        super.paintComponent(g)
    }
}
