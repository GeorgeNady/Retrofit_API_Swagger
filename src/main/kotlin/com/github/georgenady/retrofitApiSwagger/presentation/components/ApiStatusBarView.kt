package com.github.georgenady.retrofitApiSwagger.presentation.components

import com.github.georgenady.retrofitApiSwagger.MyBundle
import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.SwingConstants

class ApiStatusBarView : JPanel(BorderLayout()) {

    private val statusLabel = JBLabel(
        MyBundle.message("dashboard.ready"),
        AllIcons.General.InspectionsOK,
        SwingConstants.LEFT
    ).apply {
        border = JBUI.Borders.empty(6, 10)
        foreground = JBColor.namedColor("Label.infoForeground", JBColor.GRAY)
    }

    init {
        isOpaque = false
        add(statusLabel, BorderLayout.CENTER)
    }

    fun setMessage(text: String, icon: Icon = AllIcons.General.Information) {
        statusLabel.text = text
        statusLabel.icon = icon
    }
}
