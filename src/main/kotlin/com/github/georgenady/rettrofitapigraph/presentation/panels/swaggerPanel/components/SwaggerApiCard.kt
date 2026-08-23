package com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.main.MainToolViewModel
import com.github.georgenady.rettrofitapigraph.presentation.theme.SwaggerTheme
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BoxLayout
import javax.swing.JPanel

class SwaggerApiCard(
    private val project: Project,
    val node: ApiNode,
) : JPanel(BorderLayout()) {

    private val viewModel = project.service<MainToolViewModel>()

    private val theme = SwaggerTheme.getThemeForMethod(node.httpMethod)
    private var isHovered = false
    private var isExpanded = false

    // Dedicated components
    private val headerPanel = SwaggerApiHeaderPanel(project, node, theme)
    private val interactionPanel = SwaggerApiInteractionPanel(project, node, theme).apply {
        isOpaque = false
        alignmentX = Component.LEFT_ALIGNMENT
    }

    init {
        isOpaque = false
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        border = JBUI.Borders.empty(12)

        val mainColumn = JPanel().apply {
            isOpaque = false
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            headerPanel.alignmentX = Component.LEFT_ALIGNMENT
            interactionPanel.alignmentX = Component.LEFT_ALIGNMENT

            add(headerPanel)
            add(interactionPanel)
        }

        add(mainColumn, BorderLayout.CENTER)

        setupMouseListeners()
    }

    private fun setupMouseListeners() {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                // Clicking in the top right corner triggers expansion
                if (e.x > width - 40 && e.y < 40) {
                    viewModel.toggleExpansion(node)
                } else {
                    viewModel.selectNode(node)
                    navigateToSource()
                }
            }

            override fun mouseEntered(e: MouseEvent) {
                isHovered = true
                repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                isHovered = false
                repaint()
            }
        })
    }

    fun updateResponse(text: String) {
        interactionPanel.updateResponse(text)
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        interactionPanel.setExpanded(expanded)
        headerPanel.updateExpandState(expanded)
        revalidate()
        repaint()
    }

    private fun navigateToSource() {
        node.psiElement?.let { element ->
            val navigable = element as? Navigatable
            val canNavigate = ReadAction.compute<Boolean, Throwable> {
                navigable?.canNavigate() == true
            }
            if (canNavigate) {
                navigable?.navigate(true)
            }
        }
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = theme.backgroundColor
            g2.fillRoundRect(0, 0, width - 1, height - 1, 10, 10)
            g2.color = if (isHovered) theme.badgeColor else theme.borderColor
            g2.stroke = BasicStroke(if (isHovered) 1.5f else 1.0f)
            g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10)
        } finally {
            g2.dispose()
        }
        super.paintComponent(g)
    }
}