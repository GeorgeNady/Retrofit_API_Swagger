package com.github.georgenady.rettrofitapigraph.presentation.components

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class ApiCardListContainer(
    private val onCardClick: ((ApiNode) -> Unit)? = null
) : JPanel(BorderLayout()) {

    private val listPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(8)
    }

    init {
        val scrollPane = JBScrollPane(listPanel).apply {
            border = javax.swing.BorderFactory.createEmptyBorder()
        }
        add(scrollPane, BorderLayout.CENTER)
    }

    fun render(endpoints: List<ApiNode>) {
        listPanel.removeAll()

        // Group endpoints by their target Class / Interface Name
        val groupedEndpoints = endpoints.groupBy { it.className }

        for ((className, serviceEndpoints) in groupedEndpoints) {
            val groupPanel = ApiServiceGroupPanel(className, serviceEndpoints, onCardClick)
            listPanel.add(groupPanel)
            listPanel.add(Box.createVerticalStrut(12)) // Spacing between service groups
        }

        listPanel.revalidate()
        listPanel.repaint()
    }
}
