package com.github.georgenady.retrofitApiSwagger.presentation.components

import com.github.georgenady.retrofitApiSwagger.MyBundle
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JPanel
import javax.swing.SwingConstants

class LoadingView: JPanel(BorderLayout())  {
    init {
        isOpaque = false

        // Inner panel that manages centering its children natively via GridBagLayout
        val centerPanel = JPanel(GridBagLayout()).apply {
            isOpaque = false
        }

        val gbc = GridBagConstraints().apply {
            gridx = 0
            fill = GridBagConstraints.NONE
            anchor = GridBagConstraints.CENTER
        }

        // Row 0: Spinning Icon
        gbc.gridy = 0
        gbc.insets = Insets(0, 0, 8, 0) // Explicit 8-pixel gap beneath the icon
        centerPanel.add(AsyncProcessIcon("Scanning"), gbc)

        // Row 1: Informational Text Label
        gbc.gridy = 1
        gbc.insets = Insets(0, 0, 0, 0) // Reset padding for the baseline text element
        centerPanel.add(
            JBLabel(MyBundle.message("dashboard.scanning"), SwingConstants.CENTER),
            gbc
        )

        // Place the tightly bundled components directly into the main panel's center
        add(centerPanel, BorderLayout.CENTER)
    }
}