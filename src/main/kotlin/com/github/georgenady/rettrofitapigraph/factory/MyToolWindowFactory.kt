package com.github.georgenady.rettrofitapigraph.factory

import com.github.georgenady.rettrofitapigraph.actions.RefreshApiGraphAction
import com.github.georgenady.rettrofitapigraph.toolWindow.MyToolWindow
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class MyToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val component = myToolWindow.getComponent()
        val content = ContentFactory.getInstance().createContent(component, null, false)
        toolWindow.contentManager.addContent(content)

        // Force validation to ensure visibility
        component.revalidate()
        component.repaint()

        // Add actions to tool window header
        val actionManager = ActionManager.getInstance()
        val refreshAction = actionManager.getAction("RetrofitApiGraph.Refresh")
        val toggleAction = actionManager.getAction("RetrofitApiGraph.ToggleSidePanel")
        
        val actions = mutableListOf<AnAction>()
        refreshAction?.let { actions.add(it) }
        toggleAction?.let { actions.add(it) }
        
        if (actions.isNotEmpty()) {
            toolWindow.setTitleActions(actions)
        }
    }

    override fun shouldBeAvailable(project: Project) = true


}
