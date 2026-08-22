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
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class SwaggerApiInteractionPanel(
    private val project: Project,
    private val node: ApiNode,
    private val theme: HttpMethodTheme
) : JPanel() {

    // DIVIDER
    private val divider get() = JSeparator(JSeparator.HORIZONTAL).apply {
        maximumSize = Dimension(Int.MAX_VALUE, 1)
        foreground = JBColor(
            Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue, 80),
            Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue, 80)
        )
    }

    private val parametersLabel = JBLabel("Parameters:", JBLabel.LEFT).apply {
        font = font.deriveFont(Font.BOLD)
        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        alignmentX = LEFT_ALIGNMENT
    }

    private val parametersList = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        alignmentX = LEFT_ALIGNMENT
    }

    private val responseLabel = JBLabel("Response:", JBLabel.LEFT).apply {
        font = font.deriveFont(Font.BOLD)
        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        alignmentX = LEFT_ALIGNMENT
    }

    private val responseArea = JBTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        background = JBColor.namedColor("Editor.background", Color.DARK_GRAY)
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }

    private val tryItButton = JButton("Try It Out").apply {
        alignmentX = RIGHT_ALIGNMENT
        addActionListener { executeRequest() }
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        isVisible = false // Hidden by default
        border = JBUI.Borders.empty(10, 0, 0, 0)

        add(divider)
        add(Box.createVerticalStrut(10))
        add(parametersLabel)
        add(Box.createVerticalStrut(5))
        add(parametersList)
        add(Box.createVerticalStrut(10))
        add(tryItButton)
        add(Box.createVerticalStrut(10))
        add(divider)
        add(Box.createVerticalStrut(10))
        add(responseLabel)
        add(Box.createVerticalStrut(5))
        add(JScrollPane(responseArea).apply {
            preferredSize = Dimension(0, 150)
            alignmentX = LEFT_ALIGNMENT
        })

        setupParameters()
    }

    private fun setupParameters() {
        parametersList.removeAll()
        node.parameters.forEach { param ->
            val row = JPanel(BorderLayout()).apply {
                isOpaque = false
                val label = JBLabel("${param.name}: ${param.type}").apply {
                    font = font.deriveFont(11f)
                    border = JBUI.Borders.emptyRight(10)
                }
                add(label, BorderLayout.WEST)
                
                val field = JBTextField().apply {
                    putClientProperty("parameter_name", param.name)
                }
                add(field, BorderLayout.CENTER)
            }
            parametersList.add(row)

            if (param.location == ParameterLocation.BODY && param.fqn != null) {
                val generateUseCase = com.github.georgenady.rettrofitapigraph.domain.usecase.GenerateJsonSchemaUseCase(project)
                val mockJson = generateUseCase(param.fqn)
                val mockArea = JBTextArea(mockJson).apply {
                    rows = 5
                    font = Font(Font.MONOSPACED, Font.PLAIN, 10)
                }
                parametersList.add(Box.createVerticalStrut(4))
                parametersList.add(JBLabel("Example Value | Schema:").apply { font = font.deriveFont(Font.ITALIC, 10f) })
                parametersList.add(JScrollPane(mockArea).apply { preferredSize = Dimension(0, 80) })
            }
            parametersList.add(Box.createVerticalStrut(4))
        }
    }

    private fun executeRequest() {
        val viewModel = project.service<MainToolViewModel>()
        var finalUrl = node.path
        
        components.filterIsInstance<JPanel>().forEach { row ->
            val field = row.components.filterIsInstance<JBTextField>().firstOrNull()
            val value = field?.text ?: ""
            val name = field?.getClientProperty("parameter_name") as? String ?: ""
            if (name.isNotEmpty()) {
                finalUrl = finalUrl.replace("{$name}", value)
            }
        }
        viewModel.executeApiCall(node, finalUrl, null)
    }

    fun updateResponse(text: String) {
        responseArea.text = text
    }

    fun setExpanded(expanded: Boolean) {
        isVisible = expanded
    }
}