package com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.components.BadgeLabel
import com.github.georgenady.rettrofitapigraph.presentation.theme.HttpMethodTheme
import com.intellij.icons.AllIcons
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.ClickListener
import com.intellij.ui.JBColor
import com.intellij.ui.LightColors
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JSeparator

class SwaggerApiHeaderPanel(
    private val node: ApiNode,
    private val theme: HttpMethodTheme
) : JPanel() {

    private val expandIcon = JBLabel(AllIcons.General.ArrowDown)

    init {
        isOpaque = false
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // ROW 1: Function / Method Name + Expand Icon
        val titleRow = JPanel(BorderLayout()).apply {
            isOpaque = false
            val descLabel = JBLabel(node.methodName).apply {
                font = font.deriveFont(Font.BOLD, 13f)
                foreground = JBColor(Color(0x60, 0x67, 0x79), Color(0xA9, 0xB7, 0xC6))
            }
            add(descLabel, BorderLayout.CENTER)
            add(expandIcon, BorderLayout.EAST)
        }

        // DIVIDER
        val divider = JSeparator(JSeparator.HORIZONTAL).apply {
            maximumSize = Dimension(Int.MAX_VALUE, 1)
            foreground = JBColor(
                Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue, 80),
                Color(theme.borderColor.red, theme.borderColor.green, theme.borderColor.blue, 80)
            )
        }

        // ROW 2: HTTP Badge + Path + Copy Icon
        val pathRow = JPanel(BorderLayout()).apply {
            isOpaque = false

            val left = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
                isOpaque = false
                add(BadgeLabel(node.httpMethod, theme.badgeColor))
            }
            add(left, BorderLayout.WEST)

            val pathLabel = JBLabel(node.path.ifEmpty { "/" }).apply {
                border = JBUI.Borders.empty(0, 12, 0, 0)
                font = Font(Font.MONOSPACED, Font.BOLD, 14)
                foreground = JBColor(Color(0x3B, 0x41, 0x51), Color(0xE1, 0xE4, 0xEA))
            }
            add(pathLabel, BorderLayout.CENTER)

            // RESTORED: Copy Icon
            val copyIcon = JBLabel().apply {
                icon = AllIcons.General.Copy
                border = JBUI.Borders.empty(0, 12, 0, 0)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

                object : ClickListener() {
                    override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
                        CopyPasteManager.getInstance().setContents(StringSelection(node.path))
                        val balloon = JBPopupFactory.getInstance()
                            .createHtmlTextBalloonBuilder(
                                MyBundle.message("message.path.copied"),
                                AllIcons.Actions.Checked,
                                LightColors.BLUE,
                                null
                            )
                            .setFadeoutTime(2000)
                            .createBalloon()

                        balloon.show(RelativePoint(event), Balloon.Position.above)
                        return true
                    }
                }.installOn(this)
            }
            add(copyIcon, BorderLayout.EAST)
        }

        // Assemble with proper spacing around the divider
        add(titleRow)
        add(Box.createVerticalStrut(6))
        add(divider)
        add(Box.createVerticalStrut(8))
        add(pathRow)
    }

    fun updateExpandState(isExpanded: Boolean) {
        expandIcon.icon = if (isExpanded) AllIcons.General.ArrowUp else AllIcons.General.ArrowDown
    }
}