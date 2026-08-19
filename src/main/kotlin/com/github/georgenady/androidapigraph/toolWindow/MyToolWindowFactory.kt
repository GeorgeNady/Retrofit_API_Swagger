package com.github.georgenady.androidapigraph.toolWindow

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.georgenady.androidapigraph.services.RetrofitApiService
import com.github.georgenady.androidapigraph.ui.ApiGraphComponent
import com.intellij.icons.AllIcons

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getComponent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val project: Project) {

        private val apiService = project.service<RetrofitApiService>()
        private val graphComponent = ApiGraphComponent()
        private val panel = SimpleToolWindowPanel(true, true)

        init {
            graphComponent.onRefreshRequested = { refresh() }
            
            val actionGroup = DefaultActionGroup().apply {
                add(object : AnAction("Refresh API Graph", "Scan project for Retrofit endpoints", AllIcons.Actions.Refresh) {
                    override fun actionPerformed(e: AnActionEvent) {
                        refresh()
                    }
                })
            }

            val toolbar = ActionManager.getInstance().createActionToolbar("RetrofitApiGraphToolbar", actionGroup, true)
            toolbar.targetComponent = panel
            panel.toolbar = toolbar.component
            panel.setContent(graphComponent)

            refresh()
        }

        fun getComponent() = panel

        private fun refresh() {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Scanning for Retrofit APIs") {
                override fun run(indicator: ProgressIndicator) {
                    val endpoints = apiService.findRetrofitEndpoints()
                    
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        graphComponent.updateData(endpoints)
                    }
                }
            })
        }
    }
}
