package com.github.georgenady.rettrofitapigraph.presentation.panels.sidePanel.sections

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.panels.sidePanel.utils.SidePanelSection
import com.intellij.icons.AllIcons
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
import javax.swing.SwingConstants

class DetailsSection : SidePanelSection {

    override val title: String = MyBundle.message("details.title")

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
            val pref = super.getPreferredSize()
            val parentWidth = parent?.width ?: 0
            if (parentWidth > 0) {
                pref.width = parentWidth
            }
            return pref
        }
    }

    private val nameText = JBLabel(
        MyBundle.message("details.select_api"),
        AllIcons.General.Information,
        SwingConstants.LEFT
    ).apply {
        font = font.deriveFont(Font.BOLD, 14f)
        setCopyable(true)
        isAllowAutoWrapping = true
    }
    private val methodText = JBLabel("", AllIcons.Nodes.Method, SwingConstants.LEFT).apply {
        setCopyable(true)
    }
    private val pathText = JBLabel("", AllIcons.Nodes.Tag, SwingConstants.LEFT).apply {
        setCopyable(true)
        isAllowAutoWrapping = true
    }
    private val classText = JBLabel("", AllIcons.Nodes.Class, SwingConstants.LEFT).apply {
        setCopyable(true)
        isAllowAutoWrapping = true
    }

    private val annotationsLabel = JBLabel(
        MyBundle.message("details.annotations"),
        AllIcons.Nodes.Annotationtype,
        SwingConstants.LEFT
    ).apply {
        font = font.deriveFont(Font.BOLD, 12f)
        alignmentX = JComponent.LEFT_ALIGNMENT
    }
    private val annotationsList = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        alignmentX = JComponent.LEFT_ALIGNMENT
    }

    private val parametersLabel = JBLabel(
        MyBundle.message("details.parameters"),
        AllIcons.Nodes.Parameter,
        SwingConstants.LEFT
    ).apply {
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
            nameText.text = MyBundle.message("details.select_api")
            methodText.text = ""
            pathText.text = ""
            classText.text = ""

            methodText.isVisible = false
            pathText.isVisible = false
            classText.isVisible = false
            annotationsLabel.isVisible = false
            parametersLabel.isVisible = false
        } else {
            val isDark = !JBColor.isBright()
            val labelColor = if (isDark) "#DFE1E5" else "#1E1F22"
            val subColor = "#888888"

            nameText.text =
                "<html><body style='width: 180px'><div style='font-weight: bold; color: $labelColor; word-wrap: break-word;'>${node.methodName}</div></body></html>"
            methodText.text =
                "<html>Method: <span style='font-weight: bold; color: $labelColor'>${node.httpMethod}</span></html>"
            pathText.text = "<html>${
                MyBundle.message(
                    "details.path",
                    ""
                )
            }<span style='color: $subColor; word-wrap: break-word;'>${node.path}</span></html>"
            classText.text = "<html>${
                MyBundle.message(
                    "details.service",
                    ""
                )
            }<span style='color: $subColor; word-wrap: break-word;'>${node.className}</span></html>"

            methodText.isVisible = true
            pathText.isVisible = true
            classText.isVisible = true

            // Fix: ensure these components wrap their text even if very long
            nameText.revalidate()
            pathText.revalidate()
            classText.revalidate()

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
