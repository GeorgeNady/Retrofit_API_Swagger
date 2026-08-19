package com.github.georgenady.rettrofitapigraph.ui.sidepanel.sections

import com.github.georgenady.rettrofitapigraph.model.ApiFilterModel
import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.SidePanelSection
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.BoxLayout
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.JPanel

class FilterSection(
    private val onFilterChanged: (ApiFilterModel) -> Unit
) : SidePanelSection {

    override val title: String = "Search & Filters"
    
    private val searchField = SearchTextField().apply {
        addDocumentListener(object : com.intellij.ui.DocumentAdapter() {
            override fun textChanged(e: javax.swing.event.DocumentEvent) {
                updateFilter()
            }
        })
    }

    private val methodCheckboxes = mutableMapOf<String, JBCheckBox>()
    private val methodPanel = JPanel(GridLayout(0, 2)).apply {
        listOf("GET", "POST", "PUT", "DELETE", "PATCH").forEach { method ->
            val cb = JBCheckBox(method, true).apply {
                addActionListener { updateFilter() }
            }
            methodCheckboxes[method] = cb
            add(cb)
        }
    }

    private val moduleCombo = object : ComboBox<String>() {
        override fun getPreferredSize(): Dimension {
            val preferred = super.getPreferredSize()
            // Compute font height + compact vertical padding (e.g., 4px top/bottom)
            val fontMetrics = getFontMetrics(font)
            val textHeight = fontMetrics.height + JBUI.scale(8)

            return Dimension(preferred.width, textHeight)
        }

        override fun getMaximumSize(): Dimension {
            return Dimension(Int.MAX_VALUE, preferredSize.height)
        }
    }.apply {
        addItem("All Modules")

        // Set compact padding inside the combo box container
        border = JBUI.Borders.empty(2, 6)

        // Customize the inner cell renderer for tight vertical text margins
        setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (component is javax.swing.JComponent) {
                    component.border = JBUI.Borders.empty(2, 4)
                }
                return component
            }
        })

        addActionListener { updateFilter() }
    }

    override val component = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(4, 10)
        isOpaque = false
        
        // Ultra-compact search field wrapper
        val searchWrapper = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(searchField, BorderLayout.CENTER)
            val compactHeight = searchField.preferredSize.height
            maximumSize = java.awt.Dimension(Int.MAX_VALUE, compactHeight)
            preferredSize = java.awt.Dimension(preferredSize.width, compactHeight)
        }

        methodPanel.maximumSize = Dimension(Int.MAX_VALUE, methodPanel.preferredSize.height)
        methodPanel.isOpaque = false
        
        add(searchWrapper)
        add(javax.swing.Box.createVerticalStrut(6))
        add(methodPanel)
        add(javax.swing.Box.createVerticalStrut(6))
        add(moduleCombo)
        
        // Ensure everything is pinned to the top and doesn't scale vertically
        add(javax.swing.Box.createVerticalGlue())
    }

    fun updateModules(modules: List<String>) {
        moduleCombo.removeAllItems()
        moduleCombo.addItem("All Modules")
        modules.forEach { moduleCombo.addItem(it) }
    }

    private fun updateFilter() {
        val query = searchField.text.trim()
        val selectedMethods = methodCheckboxes.filter { it.value.isSelected }.keys
        val selectedModule = if (moduleCombo.selectedIndex > 0) moduleCombo.selectedItem as String else null
        
        onFilterChanged(ApiFilterModel(query, selectedMethods, selectedModule))
    }

    override fun onNodeSelected(node: ApiNode?) {
        // No-op for filter section
    }
}
