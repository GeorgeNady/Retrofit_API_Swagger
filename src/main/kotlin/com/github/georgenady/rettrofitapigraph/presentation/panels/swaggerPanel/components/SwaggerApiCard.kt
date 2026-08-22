package com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.components.BadgeLabel
import com.github.georgenady.rettrofitapigraph.presentation.theme.SwaggerTheme
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.pom.Navigatable
import com.intellij.ui.ClickListener
import com.intellij.ui.JBColor
import com.intellij.ui.LightColors
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JSeparator

class SwaggerApiCard(
    val node: ApiNode,
    private val onClick: ((ApiNode) -> Unit)? = null
) : JPanel(BorderLayout()) {

    private val theme = SwaggerTheme.getThemeForMethod(node.httpMethod)
    private var isHovered = false

    init {
        isOpaque = false
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        // Outer Container Padding (12px)
        border = JBUI.Borders.empty(12)

        // Main Column Container (Vertical Stack)
        val columnPanel = JPanel().apply {
            isOpaque = false
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        // ROW 1: Function / Method Name
        val row1FunctionPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            val descLabel = JBLabel(node.methodName).apply {
                font = font.deriveFont(Font.BOLD, 13f)
                foreground = JBColor(Color(0x60, 0x67, 0x79), Color(0xA9, 0xB7, 0xC6))
            }
            add(descLabel)
        }

        // DIVIDER
        val divider = JSeparator(JSeparator.HORIZONTAL).apply {
            maximumSize = Dimension(Int.MAX_VALUE, 1)
            foreground = JBColor(
                Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue, 80),
                Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue, 80)
            )
        }

        // ROW 2: HTTP Badge + Path
        val row2PathPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            add(BadgeLabel(node.httpMethod, theme.badgeColor))

            val pathLabel = JBLabel(node.path.ifEmpty { "/" }).apply {
                border = JBUI.Borders.empty(0, 12, 0, 0)
                font = Font(Font.MONOSPACED, Font.BOLD, 14)
                foreground = JBColor(Color(0x3B, 0x41, 0x51), Color(0xE1, 0xE4, 0xEA))
            }
            add(pathLabel)

            val copyIcon = JBLabel().apply {
                icon = AllIcons.General.Copy
                border = JBUI.Borders.empty(0, 12, 0, 0)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) // Changes cursor to a hand on hover

                object : ClickListener() {
                    override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
                        CopyPasteManager.getInstance().setContents(StringSelection(node.path))
                        // 2. Show an inline balloon alerter
                        val balloon = JBPopupFactory.getInstance()
                            .createHtmlTextBalloonBuilder(MyBundle.message("message.path.copied"),
                                AllIcons.General.GreenCheckmark, LightColors.BLUE, null)
                            .setFadeoutTime(2000) // Disappears after 2 seconds
                            .createBalloon()

                        balloon.show(RelativePoint(event), Balloon.Position.above)
                        return true
                    }
                }.installOn(this)
            }
            add(copyIcon)
        }

        // Assemble Column (Row 1 -> Spacing -> Divider -> Spacing -> Row 2)
        columnPanel.add(row1FunctionPanel)
        columnPanel.add(Box.createVerticalStrut(6))
        columnPanel.add(divider)
        columnPanel.add(Box.createVerticalStrut(8))
        columnPanel.add(row2PathPanel)

        add(columnPanel, BorderLayout.CENTER)

        // Mouse Navigation Listener
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onClick?.invoke(node)
                node.psiElement?.let { element ->
                    val navigatable = element as? Navigatable
                    val canNavigate = ReadAction.compute<Boolean, Throwable> {
                        navigatable?.canNavigate() == true
                    }
                    if (canNavigate) {
                        navigatable?.navigate(true)
                    }
                }
            }

            override fun mouseEntered(e: MouseEvent) {
                isHovered = true
                repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                isHovered = false
                repaint()
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            // Draw Background Surface
            g2.color = theme.backgroundColor
            g2.fillRoundRect(0, 0, width - 1, height - 1, 10, 10)

            // Draw Border
            g2.color = if (isHovered) theme.badgeColor else theme.borderColor
            g2.stroke = BasicStroke(if (isHovered) 1.5f else 1.0f)
            g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10)
        } finally {
            g2.dispose()
        }
        super.paintComponent(g)
    }

    override fun getPreferredSize(): Dimension {
        val preferred = super.getPreferredSize()
        return Dimension(preferred.width, preferred.height)
    }
}