package com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.domain.model.ParameterLocation
import com.github.georgenady.rettrofitapigraph.presentation.main.MainToolViewModel
import com.github.georgenady.rettrofitapigraph.presentation.theme.HttpMethodTheme
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class SwaggerApiInteractionPanel(
    private val project: Project,
    private val node: ApiNode,
    private val theme: HttpMethodTheme
) : JPanel() {

    // DIVIDER
    private val primaryDivider = JSeparator(JSeparator.HORIZONTAL).apply {
        maximumSize = Dimension(Int.MAX_VALUE, 1)
        alignmentX = Component.LEFT_ALIGNMENT
        foreground = JBColor(
            Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue),
            Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue)
        )
    }

    private val divider = JSeparator(JSeparator.HORIZONTAL).apply {
        maximumSize = Dimension(Int.MAX_VALUE, 1)
        alignmentX = Component.LEFT_ALIGNMENT
        foreground = JBColor(
            Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue, 80),
            Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue, 80)
        )
    }

    private val parametersLabel = JBLabel("Parameters:", JBLabel.LEFT).apply {
        font = font.deriveFont(Font.BOLD)
        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        alignmentX = Component.LEFT_ALIGNMENT
    }

    private val parametersList = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        alignmentX = Component.LEFT_ALIGNMENT
    }

    private val responseLabel = JBLabel("Response:", JBLabel.LEFT).apply {
        font = font.deriveFont(Font.BOLD)
        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        alignmentX = Component.LEFT_ALIGNMENT
    }

    private val responseArea = JBTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        background = JBColor.namedColor("Editor.background", Color.DARK_GRAY)
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }

    private val tryItButton = JButton("Try It Out").apply {
        addActionListener { executeRequest() }
    }

    init {
        isOpaque = false
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isVisible = false
        border = JBUI.Borders.empty(10, 0, 0, 0)

        val buttonWrapper = JPanel(BorderLayout()).apply {
            isOpaque = false
            alignmentX = Component.LEFT_ALIGNMENT
            add(tryItButton, BorderLayout.EAST)
        }

        add(primaryDivider)
        add(Box.createVerticalStrut(10))

        // CONDITIONAL CHECK: Only add parameters section if parameters exist
        if (node.parameters.isNotEmpty()) {
            add(parametersLabel)
            add(Box.createVerticalStrut(5))
            add(parametersList)
            add(Box.createVerticalStrut(10))
            setupParameters()
        }

        add(buttonWrapper)
        add(Box.createVerticalStrut(10))
        add(divider)
        add(Box.createVerticalStrut(10))
        add(responseLabel)
        add(Box.createVerticalStrut(5))
        add(JScrollPane(responseArea).apply {
            preferredSize = Dimension(0, 150)
            alignmentX = Component.LEFT_ALIGNMENT
        })
    }

    private fun setupParameters() {
        parametersList.removeAll()
        node.parameters.forEach { param ->
            // Pass `node` as the second argument here!
            val rowComponent = SwaggerApiParameterRow(project, node, param)

            parametersList.add(rowComponent)
            parametersList.add(Box.createVerticalStrut(4))
        }
    }

    private fun executeRequest() {
        val viewModel = project.service<MainToolViewModel>()
        var finalUrl = node.path

        // Cleanly extract data using our custom component's helper method
        parametersList.components.filterIsInstance<SwaggerApiParameterRow>().forEach { row ->
            val data = row.getParameterData()
            if (data != null) {
                val (name, value) = data
                finalUrl = finalUrl.replace("{$name}", value)
            }
        }

        viewModel.executeApiCall(node, finalUrl, null)
    }

//    private fun executeRequest() {
//        val viewModel = project.service<MainToolViewModel>()
//        var finalUrl = node.path
//
//        parametersList.components.filterIsInstance<JPanel>().forEach { row ->
//            val field = row.components.filterIsInstance<JBTextField>().firstOrNull()
//            val value = field?.text ?: ""
//            val name = field?.getClientProperty("parameter_name") as? String ?: ""
//            if (name.isNotEmpty()) {
//                finalUrl = finalUrl.replace("{$name}", value)
//            }
//        }
//        viewModel.executeApiCall(node, finalUrl, null)
//    }

    fun updateResponse(text: String) {
        responseArea.text = text
    }

    fun setExpanded(expanded: Boolean) {
        isVisible = expanded
    }
}