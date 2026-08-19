package com.github.georgenady.rettrofitapigraph.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.georgenady.rettrofitapigraph.services.RetrofitApiService
import com.github.georgenady.rettrofitapigraph.ui.ApiGraphComponent
import java.awt.BorderLayout
import javax.swing.JPanel

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getComponent(), null, false)
        toolWindow.contentManager.addContent(content)
        
        val actionGroup = DefaultActionGroup().apply {
            add(object : AnAction("Refresh API Graph", "Scan project for Retrofit endpoints", AllIcons.Actions.Refresh) {
                override fun actionPerformed(e: AnActionEvent) {
                    myToolWindow.refresh()
                }
            })
        }
        toolWindow.setTitleActions(listOf(actionGroup.getChildren(null)[0]))
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val project: Project) {

        private val apiService = project.service<RetrofitApiService>()
        private val graphComponent = ApiGraphComponent(project)
        private val mainPanel = JPanel(BorderLayout())

        init {
            graphComponent.onRefreshRequested = { refresh() }
            mainPanel.add(graphComponent, BorderLayout.CENTER)
            refresh()
        }

        fun getComponent() = mainPanel

        fun refresh() {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Scanning for Retrofit APIs") {
                override fun run(indicator: ProgressIndicator) {
                    val result = apiService.findRetrofitEndpoints()
                    ApplicationManager.getApplication().invokeLater {
                        graphComponent.updateData(result.endpoints)
                    }
                }
            })
        }
    }
}
