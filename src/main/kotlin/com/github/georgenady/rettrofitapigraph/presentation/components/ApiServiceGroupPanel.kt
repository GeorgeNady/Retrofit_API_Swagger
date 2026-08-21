package com.github.georgenady.rettrofitapigraph.presentation.components

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class ApiServiceGroupPanel(
    val className: String,
    endpoints: List<ApiNode>,
    onCardClick: ((ApiNode) -> Unit)? = null
) : JPanel(BorderLayout()) {

    init {
        isOpaque = false
        border = JBUI.Borders.emptyBottom(12)

        // Class / Service Header
        val headerPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.empty(4, 2, 8, 2)
            
            val titleLabel = JBLabel(className).apply {
                font = font.deriveFont(Font.BOLD, 14f)
                foreground = JBColor.namedColor("Label.foreground", JBColor.GRAY)
            }
            add(titleLabel, BorderLayout.WEST)
        }

        // Child Cards Container
        val cardsContainer = JPanel().apply {
            isOpaque = false
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            
            for (node in endpoints) {
                add(ApiCardComponent(node, onCardClick))
                add(Box.createVerticalStrut(6)) // Spacing between cards inside group
            }
        }

        add(headerPanel, BorderLayout.NORTH)
        add(cardsContainer, BorderLayout.CENTER)
    }
}
