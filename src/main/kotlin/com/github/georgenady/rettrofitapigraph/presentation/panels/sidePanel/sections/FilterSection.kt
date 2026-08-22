package com.github.georgenady.rettrofitapigraph.presentation.panels.sidePanel.sections

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.data.parser.utils.RetrofitConstants
import com.github.georgenady.rettrofitapigraph.domain.model.ApiFilterModel
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.panels.sidePanel.utils.SidePanelSection
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class FilterSection(
    private val onFilterChanged: (ApiFilterModel) -> Unit
) : SidePanelSection {

    override val title: String = MyBundle.message("filter.title")

    private var isUpdatingCombo = false

    private val searchField = SearchTextField().apply {
        addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                updateFilter()
            }
        })
    }

    private val methodCheckboxes = mutableMapOf<String, JBCheckBox>()
    private val methodPanel = JPanel(GridLayout(0, 2, 4, 4)).apply {
        isOpaque = false
        alignmentX = Component.LEFT_ALIGNMENT
        RetrofitConstants.HTTP_METHODS.forEach { method ->
            val checkBox = JBCheckBox(method, true).apply {
                isOpaque = false
                addActionListener { updateFilter() }
            }
            methodCheckboxes[method] = checkBox
            add(checkBox)
        }
    }

    private val moduleComboLabel = JBLabel(MyBundle.message("filter.moduleCompo.label")).apply {
        font = font.deriveFont(font.size - 1f)
        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        alignmentX = Component.LEFT_ALIGNMENT
    }

    private val moduleCombo = object : ComboBox<String>() {
        override fun getPreferredSize(): Dimension {
            val preferred = super.getPreferredSize()
            val fontMetrics = getFontMetrics(font)
            val textHeight = fontMetrics.height + JBUI.scale(8)
            return Dimension(preferred.width, textHeight)
        }

        override fun getMaximumSize(): Dimension {
            return Dimension(Int.MAX_VALUE, preferredSize.height)
        }
    }.apply {
        addItem(MyBundle.message("filter.all_modules"))
        border = JBUI.Borders.empty(2, 6)
        alignmentX = Component.LEFT_ALIGNMENT

        setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val comp =
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (comp is JComponent) {
                    comp.border = JBUI.Borders.empty(2, 4)
                }
                return comp
            }
        })

        addActionListener {
            if (!isUpdatingCombo) {
                updateFilter()
            }
        }
    }

    private val customAnnotationsLabel = JBLabel(MyBundle.message("filter.annotations.label"), JBLabel.LEFT).apply {
        font = font.deriveFont(font.size - 1f)
        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        alignmentX = Component.LEFT_ALIGNMENT
    }


    private val customAnnotationsField = JBTextField().apply {
        emptyText.text = MyBundle.message("filter.annotations.empty_text")
        document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                updateFilter()
            }
        })
    }

    override val component = JPanel(BorderLayout()).apply {
        isOpaque = false

        val contentContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(8, 10)
            isOpaque = false

            // Search Bar Wrapper
            val searchWrapper = JPanel(BorderLayout()).apply {
                isOpaque = false
                add(searchField, BorderLayout.CENTER)
                val compactHeight = searchField.preferredSize.height
                maximumSize = Dimension(Int.MAX_VALUE, compactHeight)
                alignmentX = Component.LEFT_ALIGNMENT
            }

            val annotationsWrapper = JPanel(BorderLayout()).apply {
                isOpaque = false
                add(customAnnotationsField, BorderLayout.CENTER)
                maximumSize = Dimension(Int.MAX_VALUE, customAnnotationsField.preferredSize.height)
                alignmentX = Component.LEFT_ALIGNMENT
            }

            methodPanel.maximumSize = Dimension(Int.MAX_VALUE, methodPanel.preferredSize.height)

            add(searchWrapper)
            add(Box.createVerticalStrut(10))
            add(methodPanel)
            add(Box.createVerticalStrut(10))
            add(moduleComboLabel)
            add(Box.createVerticalStrut(4))
            add(moduleCombo)
            add(Box.createVerticalStrut(10))
            add(customAnnotationsLabel)
            add(Box.createVerticalStrut(4))
            add(annotationsWrapper)
        }

        // Anchored too NORTH to keep heights fixed without vertical glue
        add(contentContainer, BorderLayout.NORTH)
    }

    fun updateModules(modules: List<String>) {
        isUpdatingCombo = true
        try {
            moduleCombo.removeAllItems()
            moduleCombo.addItem(MyBundle.message("filter.all_modules"))
            modules.distinct().sorted().forEach { moduleCombo.addItem(it) }
        } finally {
            isUpdatingCombo = false
        }
    }

    private fun updateFilter() {
        val query = searchField.text.trim()
        val selectedMethods = methodCheckboxes.filter { it.value.isSelected }.keys
        val selectedModule =
            if (moduleCombo.selectedIndex > 0) moduleCombo.selectedItem as? String else null

        val customAnnotations = customAnnotationsField.text
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        onFilterChanged(ApiFilterModel(query, selectedMethods, selectedModule, customAnnotations))
    }

    override fun onNodeSelected(node: ApiNode?) {
        // Filter section does not react to node selection
    }
}
