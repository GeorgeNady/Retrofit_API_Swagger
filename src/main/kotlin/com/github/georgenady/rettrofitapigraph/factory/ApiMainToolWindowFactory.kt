package com.github.georgenady.rettrofitapigraph.factory

import com.github.georgenady.rettrofitapigraph.services.ApiStateService
import com.github.georgenady.rettrofitapigraph.ui.ApiMainDashboard
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ApiMainToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val dashboard = ApiMainDashboard(project)
        val content = ContentFactory.getInstance().createContent(dashboard, null, false)
        toolWindow.contentManager.addContent(content)

        // Force validation to ensure visibility
        dashboard.revalidate()
        dashboard.repaint()

        // Add actions to tool window header
        val actionManager = ActionManager.getInstance()
        val refreshAction = actionManager.getAction("RetrofitApiGraph.Refresh")
        val switchAction = actionManager.getAction("RetrofitApiGraph.SwitchView")
        val toggleAction = actionManager.getAction("RetrofitApiGraph.ToggleSidePanel")
        
        val actions = mutableListOf<AnAction>()
        refreshAction?.let { actions.add(it) }
        switchAction?.let { actions.add(it) }
        toggleAction?.let { actions.add(it) }
        
        if (actions.isNotEmpty()) {
            toolWindow.setTitleActions(actions)
        }

        // Initial scan
        project.service<ApiStateService>().refresh()
    }

    override fun shouldBeAvailable(project: Project) = true
}
