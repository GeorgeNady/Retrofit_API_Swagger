package com.github.georgenady.rettrofitapigraph.actions

import com.github.georgenady.rettrofitapigraph.toolWindow.MyToolWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class ToggleSidePanelAction : AnAction("Toggle Side Panel", "Show or hide the feature panel", AllIcons.General.Filter) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("RetrofitApiGraph") ?: return
        val content = toolWindow.contentManager.getContent(0) ?: return
        val component = content.component
        val myToolWindow = component.getClientProperty("MyToolWindow") as? MyToolWindow
        myToolWindow?.toggleSidePanel()
    }
}
