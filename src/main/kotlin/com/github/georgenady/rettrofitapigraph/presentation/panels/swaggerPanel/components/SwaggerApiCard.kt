package com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.main.MainToolViewModel
import com.github.georgenady.rettrofitapigraph.presentation.theme.SwaggerTheme
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BoxLayout
import javax.swing.JPanel

class SwaggerApiCard(
    private val project: Project,
    val node: ApiNode,
) : JPanel(BorderLayout()) {

    private val viewModel = project.service<MainToolViewModel>()
    private var subscriptionJob: Job? = null

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

    override fun addNotify() {
        super.addNotify()
        subscriptionJob = viewModel.viewModelScope.launch(Dispatchers.Main) {
            // Observe Expansion
            launch {
                viewModel.uiState
                    .map { it.expandedNode?.signature == node.signature }
                    .distinctUntilChanged()
                    .collect { shouldExpand ->
                        setExpanded(shouldExpand)
                        if (shouldExpand) {
                            scrollToVisible()
                        }
                    }
            }
            
            // Observe Results
            launch {
                val sig = "${node.className}.${node.methodName}"
                viewModel.uiState
                    .map { it.requestResults[sig] }
                    .distinctUntilChanged()
                    .collect { result ->
                        result?.let { updateResponse(it) }
                    }
            }
        }
    }

    override fun removeNotify() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        super.removeNotify()
    }

    private fun scrollToVisible() {
        javax.swing.SwingUtilities.invokeLater {
            val rect = bounds
            var curr: Container? = parent
            while (curr != null && curr !is javax.swing.JViewport) {
                val p = curr.parent
                if (p != null) {
                    rect.x += curr.bounds.x
                    rect.y += curr.bounds.y
                }
                curr = p
            }
            (curr as? javax.swing.JViewport)?.scrollRectToVisible(rect)
        }
    }

    private fun setupMouseListeners() {
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (!e.isConsumed) {
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