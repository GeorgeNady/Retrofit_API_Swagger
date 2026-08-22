package com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components.SwaggerServiceGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import kotlin.collections.iterator

class SwaggerPanel(
    private val project: Project,
    private val onCardClick: ((ApiNode) -> Unit)? = null
) : JPanel(BorderLayout()) {

    private val listPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(8)
    }

    init {
        val scrollPane = JBScrollPane(listPanel).apply {
            border = BorderFactory.createEmptyBorder()
        }
        add(scrollPane, BorderLayout.CENTER)
    }

    fun render(endpoints: List<ApiNode>) {
        listPanel.removeAll()

        // Group endpoints by their target Class / Interface Name
        val groupedEndpoints = endpoints.groupBy { it.className }

        for ((className, serviceEndpoints) in groupedEndpoints) {
            val groupPanel = SwaggerServiceGroup(className, serviceEndpoints, onCardClick)
            listPanel.add(groupPanel)
            listPanel.add(Box.createVerticalStrut(12)) // Spacing between service groups
        }

        listPanel.revalidate()
        listPanel.repaint()
    }
}
