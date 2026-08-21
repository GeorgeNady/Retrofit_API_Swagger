package com.github.georgenady.rettrofitapigraph.presentation.sidepanel

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import javax.swing.JComponent

interface SidePanelSection {
    val title: String
    val component: JComponent
    fun onNodeSelected(node: ApiNode?)
}
