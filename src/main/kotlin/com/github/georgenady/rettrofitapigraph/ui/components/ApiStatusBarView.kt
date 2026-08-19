package com.github.georgenady.rettrofitapigraph.ui.components

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

class ApiStatusBarView : JPanel(BorderLayout()) {

    private val statusLabel = JBLabel("Ready").apply {
        horizontalAlignment = SwingConstants.LEFT
        border = JBUI.Borders.empty(6, 10)
        foreground = JBColor.namedColor("Label.infoForeground", JBColor.GRAY)
    }

    init {
        isOpaque = false
        add(statusLabel, BorderLayout.CENTER)
    }

    fun setMessage(text: String) {
        statusLabel.text = text
    }
}