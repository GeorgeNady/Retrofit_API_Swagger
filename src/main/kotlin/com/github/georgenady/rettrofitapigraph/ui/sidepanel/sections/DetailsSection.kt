package com.github.georgenady.rettrofitapigraph.ui.sidepanel.sections

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.SidePanelSection
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Rectangle
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.Scrollable

class DetailsSection : SidePanelSection {

    override val title: String = "API Details"

    /**
     * A JTextArea that implements Scrollable to strictly track the viewport width.
     * This forces soft text wrapping up to the visible boundary and eliminates
     * infinite horizontal scrolling.
     */
    private class ResponsiveWrappedTextArea(
        defaultText: String = "",
        fontStyle: Int = Font.PLAIN,
        fontSize: Float = 12f
    ) : JTextArea(defaultText), Scrollable {

        init {
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
            isOpaque = false
            isFocusable = false
            font = font.deriveFont(fontStyle, fontSize)
            foreground = JBColor.namedColor("Label.foreground", JBColor.GRAY)
            border = JBUI.Borders.empty()
            alignmentX = LEFT_ALIGNMENT
        }

        // CRITICAL FIX 1: Force component width to match parent viewport width
        override fun getScrollableTracksViewportWidth(): Boolean = true

        // CRITICAL FIX 2: Do not force viewport height
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

        override fun getPreferredSize(): Dimension {
            val parentWidth = parent?.width ?: 0
            if (parentWidth > 0) {
                // Dynamically re-calculate height based on current parent width
                setSize(parentWidth, Short.MAX_VALUE.toInt())
            }
            val pref = super.getPreferredSize()
            return Dimension(if (parentWidth > 0) parentWidth else pref.width, pref.height)
        }
    }

    private val nameText = ResponsiveWrappedTextArea("Select an API...", Font.BOLD, 14f)
    private val methodText = ResponsiveWrappedTextArea("", Font.PLAIN, 12f)
    private val pathText = ResponsiveWrappedTextArea("", Font.PLAIN, 12f)
    private val classText = ResponsiveWrappedTextArea("", Font.PLAIN, 12f)

    private val annotationsLabel = JBLabel("Annotations:").apply {
        font = font.deriveFont(Font.BOLD, 12f)
        alignmentX = JComponent.LEFT_ALIGNMENT
    }
    private val annotationsList = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        alignmentX = JComponent.LEFT_ALIGNMENT
    }

    private val parametersLabel = JBLabel("Parameters:").apply {
        font = font.deriveFont(Font.BOLD, 12f)
        alignmentX = JComponent.LEFT_ALIGNMENT
    }
    private val parametersList = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        alignmentX = JComponent.LEFT_ALIGNMENT
    }

    override val component = JPanel(BorderLayout()).apply {
        isOpaque = false

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(10)
            isOpaque = false

            add(nameText)
            add(Box.createVerticalStrut(8))
            add(methodText)
            add(Box.createVerticalStrut(4))
            add(pathText)
            add(Box.createVerticalStrut(4))
            add(classText)

            add(Box.createVerticalStrut(12))
            add(annotationsLabel)
            add(Box.createVerticalStrut(4))
            add(annotationsList)

            add(Box.createVerticalStrut(12))
            add(parametersLabel)
            add(Box.createVerticalStrut(4))
            add(parametersList)
        }

        add(contentPanel, BorderLayout.NORTH)

        // Force layout recalculation on window resize
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                contentPanel.revalidate()
                contentPanel.repaint()
            }
        })
    }

    override fun onNodeSelected(node: ApiNode?) {
        annotationsList.removeAll()
        parametersList.removeAll()

        if (node == null) {
            nameText.text = "Select an API..."
            methodText.text = ""
            pathText.text = ""
            classText.text = ""

            methodText.isVisible = false
            pathText.isVisible = false
            classText.isVisible = false
            annotationsLabel.isVisible = false
            parametersLabel.isVisible = false
        } else {
            nameText.text = node.methodName
            methodText.text = "Method: ${node.httpMethod}"
            pathText.text = "Path: ${node.path}"
            classText.text = "Service: ${node.className}"

            methodText.isVisible = true
            pathText.isVisible = true
            classText.isVisible = true

            annotationsLabel.isVisible = node.annotations.isNotEmpty()
            node.annotations.forEach { anno ->
                val args = anno.arguments.entries.joinToString(", ") { "${it.key}=${it.value}" }
                val annoStr = "@${anno.name}${if (args.isNotEmpty()) "($args)" else ""}"

                val textComp = ResponsiveWrappedTextArea(annoStr, Font.PLAIN, 12f).apply {
                    border = JBUI.Borders.empty(2, 4)
                }
                annotationsList.add(textComp)
                annotationsList.add(Box.createVerticalStrut(2))
            }

            parametersLabel.isVisible = node.parameters.isNotEmpty()
            node.parameters.forEach { param ->
                val paramStr = "${param.name}: ${param.type}"

                val textComp = ResponsiveWrappedTextArea(paramStr, Font.PLAIN, 12f).apply {
                    border = JBUI.Borders.empty(2, 4)
                }
                parametersList.add(textComp)
                parametersList.add(Box.createVerticalStrut(2))
            }
        }

        component.revalidate()
        component.repaint()
    }
}
