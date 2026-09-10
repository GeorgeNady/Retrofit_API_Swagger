package com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel

import com.github.georgenady.retrofitApiSwagger.MyBundle
import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class GraphOverlayToolbar(
    private val onHandToggled: (Boolean) -> Unit,
    private val onZoomIn: () -> Unit,
    private val onZoomOut: () -> Unit,
    private val onZoomReset: () -> Unit,
    private val onZoomFit: () -> Unit
) : JPanel() {

    private var isHandActive = false

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        
        // Single separate panel for Hand Tool
        val handPanel = createGroupPanel().apply {
            add(createButton(AllIcons.Toolwindows.ToolWindowPalette, MyBundle.message("action.pan_mode")) {
                isHandActive = !isHandActive
                it.isContentAreaFilled = isHandActive
                it.background = if (isHandActive) JBColor.namedColor("Button.startBackground", Color(0x4B, 0x50, 0x52)) else Color(0,0,0,0)
                onHandToggled(isHandActive)
            })
        }

        // Group panel for Zoom actions
        val zoomPanel = createGroupPanel().apply {
            add(createButton(AllIcons.General.Add, MyBundle.message("action.zoom_in.text")) { onZoomIn() })
            add(createButton(AllIcons.General.Remove, MyBundle.message("action.zoom_out.text")) { onZoomOut() })
            add(createButton(null, MyBundle.message("action.zoom_actual.text")) { onZoomReset() }.apply { text = "1:1"; font = font.deriveFont(9f) })
            add(createButton(AllIcons.General.FitContent, MyBundle.message("action.zoom_fit.text")) { onZoomFit() })
        }

        add(handPanel)
        add(Box.createVerticalStrut(8))
        add(zoomPanel)
    }

    private fun createGroupPanel() = object : JPanel() {
        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = true
            background = JBColor.namedColor("Panel.background", Color(60, 63, 65, 200))
            border = BorderFactory.createLineBorder(JBColor.namedColor("Divider.color", Color(80, 80, 80)), 1, true)
        }
        
        override fun paintComponent(g: Graphics) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = background
            g2.fillRoundRect(0, 0, width, height, 10, 10)
            g2.dispose()
            super.paintComponent(g)
        }
    }.apply { isOpaque = false }

    private fun createButton(icon: Icon?, tooltip: String, action: (JButton) -> Unit): JButton {
        return JButton(icon).apply {
            toolTipText = tooltip
            isContentAreaFilled = false
            isBorderPainted = false
            isFocusPainted = false
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            margin = JBUI.insets(4)
            preferredSize = Dimension(32, 32)
            maximumSize = Dimension(32, 32)
            
            addActionListener { action(this) }
            
            addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent) {
                    if (!isSelected) isContentAreaFilled = true
                    background = JBColor.namedColor("Button.hoverBackground", Color(255, 255, 255, 30))
                }
                override fun mouseExited(e: MouseEvent) {
                    if (!isSelected) isContentAreaFilled = false
                }
            })
        }
    }
}
