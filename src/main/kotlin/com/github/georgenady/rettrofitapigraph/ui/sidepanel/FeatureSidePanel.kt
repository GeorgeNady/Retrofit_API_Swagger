package com.github.georgenady.rettrofitapigraph.ui.sidepanel

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.services.ApiStateService
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel

class FeatureSidePanel(private val project: Project) : JBPanel<FeatureSidePanel>(BorderLayout()) {

    private val sections = mutableListOf<SidePanelSection>()
    private val contentPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
    }

    init {
        val scrollPane = JBScrollPane(contentPanel).apply {
            border = BorderFactory.createEmptyBorder()
            viewport.isOpaque = false
            isOpaque = false
        }
        add(scrollPane, BorderLayout.CENTER)
        border = JBUI.Borders.customLine(JBColor.border(), 0, 1, 0, 0)

        project.messageBus.connect().subscribe(ApiStateService.TOPIC, object : ApiStateService.ApiStateListener {
            override fun onNodeSelected(node: ApiNode?) {
                notifyNodeSelected(node)
            }
        })
    }

    fun addSection(section: SidePanelSection) {
        sections.add(section)
        val sectionWrapper = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()),
                section.title
            )
            add(section.component, BorderLayout.CENTER)
        }
        contentPanel.add(sectionWrapper)
        contentPanel.revalidate()
    }

    fun notifyNodeSelected(node: ApiNode?) {
        sections.forEach { it.onNodeSelected(node) }
    }
}
