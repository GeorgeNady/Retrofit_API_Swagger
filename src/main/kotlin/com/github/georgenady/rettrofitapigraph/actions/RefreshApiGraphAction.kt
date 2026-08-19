package com.github.georgenady.rettrofitapigraph.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.github.georgenady.rettrofitapigraph.factory.MyToolWindowFactory
import com.github.georgenady.rettrofitapigraph.toolWindow.MyToolWindow
import com.intellij.icons.AllIcons

class RefreshApiGraphAction : AnAction("Refresh API Graph", "Scan project for Retrofit endpoints", AllIcons.Actions.Refresh) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("RetrofitApiGraph") ?: return
        
        // Find the MyToolWindow instance from the content
        val content = toolWindow.contentManager.getContent(0) ?: return
        val component = content.component
        
        // We'll need a way to get the MyToolWindow instance. 
        // For now, let's assume we can trigger a refresh if we have a reference.
        // I'll update MyToolWindow to be stored in the component's client property.
        val myToolWindow = component.getClientProperty("MyToolWindow") as? MyToolWindow
        myToolWindow?.refresh()
    }
}
