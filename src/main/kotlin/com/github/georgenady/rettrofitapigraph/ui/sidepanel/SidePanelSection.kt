package com.github.georgenady.rettrofitapigraph.ui.sidepanel

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import javax.swing.JComponent

interface SidePanelSection {
    val title: String
    val component: JComponent
    fun onNodeSelected(node: ApiNode?)
}
