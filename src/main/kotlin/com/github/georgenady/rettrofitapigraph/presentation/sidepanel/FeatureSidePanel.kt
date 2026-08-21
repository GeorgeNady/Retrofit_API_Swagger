package com.github.georgenady.rettrofitapigraph.presentation.sidepanel

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.Scrollable

class FeatureSidePanel(project: Project) : JBPanel<FeatureSidePanel>(BorderLayout()) {

    private val sections = mutableListOf<SidePanelSection>()

    // CRITICAL FIX: Implement Scrollable so contentPanel NEVER expands past the scroll pane viewport width
    private val contentPanel = object : JPanel(), Scrollable {
        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        override fun getScrollableTracksViewportWidth(): Boolean = true

        override fun getScrollableTracksViewportHeight(): Boolean = false

        override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

        override fun getScrollableUnitIncrement(
            visibleRect: Rectangle?,
            orientation: Int,
            direction: Int
        ): Int = 10

        override fun getScrollableBlockIncrement(
            visibleRect: Rectangle?,
            orientation: Int,
            direction: Int
        ): Int = 50
    }

    init {
        val scrollPane = JBScrollPane(contentPanel).apply {
            border = BorderFactory.createEmptyBorder()
            viewport.isOpaque = false
            isOpaque = false
            // Disable horizontal scrollbar completely on the side panel
            horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }

        add(scrollPane, BorderLayout.CENTER)
        border = JBUI.Borders.customLine(JBColor.border(), 0, 1, 0, 0)
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
        contentPanel.repaint()
    }

    fun notifyNodeSelected(node: ApiNode?) {
        sections.forEach { it.onNodeSelected(node) }
    }
}
