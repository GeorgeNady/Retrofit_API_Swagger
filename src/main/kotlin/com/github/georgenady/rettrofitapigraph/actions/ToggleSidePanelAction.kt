package com.github.georgenady.rettrofitapigraph.actions

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.ui.ApiMainDashboard
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class ToggleSidePanelAction : AnAction(
    MyBundle.message("action.toggle_side_panel.text"),
    MyBundle.message("action.toggle_side_panel.description"),
    AllIcons.General.Filter
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("RetrofitApiGraph") ?: return
        val content = toolWindow.contentManager.getContent(0) ?: return
        val dashboard = content.component.getClientProperty("ApiMainDashboard") as? ApiMainDashboard
        dashboard?.toggleSidePanel()
    }
}
