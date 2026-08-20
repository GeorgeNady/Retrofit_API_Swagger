package com.github.georgenady.rettrofitapigraph.ui.components

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.Cursor
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class ApiEmptyStateView(
    private val onScanRequested: (() -> Unit)? = null
) : JPanel(GridBagLayout()) {

    init {
        isOpaque = false
        border = JBUI.Borders.empty(20)

        val gbc = GridBagConstraints().apply {
            gridx = 0
            gridy = GridBagConstraints.RELATIVE
            anchor = GridBagConstraints.CENTER
            insets = JBUI.insets(4)
        }

        val titleLabel = JBLabel(MyBundle.message("empty.title"), SwingConstants.CENTER).apply {
            font = font.deriveFont(Font.BOLD, 16f)
            foreground = JBColor.namedColor("Label.infoForeground", JBColor.GRAY)
        }

        val subtitleLabel = JBLabel(MyBundle.message("empty.subtitle"), SwingConstants.CENTER).apply {
            font = font.deriveFont(Font.PLAIN, 12f)
            foreground = JBColor.namedColor("Label.subtextForeground", JBColor.GRAY)
        }

        val scanButton = JButton(MyBundle.message("action.scan_project"), AllIcons.Actions.Refresh).apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addActionListener { onScanRequested?.invoke() }
        }

        add(titleLabel, gbc)
        add(subtitleLabel, gbc)
        gbc.insets = JBUI.insetsTop(12)
        add(scanButton, gbc)
    }
}
