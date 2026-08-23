package com.github.georgenady.retrofitApiSwagger.presentation.panels.sidePanel.utils

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode
import javax.swing.JComponent

interface SidePanelSection {
    val title: String
    val component: JComponent
    fun onNodeSelected(node: ApiNode?)
}