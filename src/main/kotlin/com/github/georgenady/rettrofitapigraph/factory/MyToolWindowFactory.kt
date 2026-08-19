package com.github.georgenady.rettrofitapigraph.factory

import com.github.georgenady.rettrofitapigraph.actions.RefreshApiGraphAction
import com.github.georgenady.rettrofitapigraph.toolWindow.MyToolWindow
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class MyToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content =
            ContentFactory.getInstance().createContent(myToolWindow.getComponent(), null, false)
        toolWindow.contentManager.addContent(content)

        // Add Refresh action to tool window header
        val actionManager = ActionManager.getInstance()
        val refreshAction = actionManager.getAction(RefreshApiGraphAction::class.java.canonicalName)
        if (refreshAction != null) {
            toolWindow.setTitleActions(listOf(refreshAction))
        }
    }

    override fun shouldBeAvailable(project: Project) = true


}
