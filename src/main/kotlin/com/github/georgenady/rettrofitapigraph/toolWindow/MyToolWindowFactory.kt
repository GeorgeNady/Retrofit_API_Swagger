package com.github.georgenady.rettrofitapigraph.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.github.georgenady.rettrofitapigraph.services.RetrofitApiService
import com.github.georgenady.rettrofitapigraph.ui.ApiGraphComponent
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

class MyToolWindowFactory : ToolWindowFactory, DumbAware {

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
        private val cardLayout = CardLayout()
        private val mainPanel = JPanel(cardLayout)

        init {
            val loadingPanel = JPanel(BorderLayout()).apply {
                val centerPanel = JPanel(BorderLayout())
                centerPanel.add(AsyncProcessIcon("Scanning"), BorderLayout.NORTH)
                centerPanel.add(JBLabel("Scanning Project for Retrofit APIs...", SwingConstants.CENTER), BorderLayout.SOUTH)
                add(centerPanel, BorderLayout.CENTER)
            }

            graphComponent.onRefreshRequested = { refresh() }
            
            mainPanel.add(loadingPanel, "LOADING")
            mainPanel.add(graphComponent, "GRAPH")
            
            refresh()
        }

        fun getComponent() = mainPanel

        fun refresh() {
            cardLayout.show(mainPanel, "LOADING")
            
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Scanning for Retrofit APIs") {
                override fun run(indicator: ProgressIndicator) {
                    val result = apiService.findRetrofitEndpoints()
                    ApplicationManager.getApplication().invokeLater {
                        graphComponent.updateData(result.endpoints)
                        graphComponent.setStatus("Scanned ${result.filesScanned} files. Found ${result.endpoints.size} APIs.")
                        cardLayout.show(mainPanel, "GRAPH")
                    }
                }
            })
        }
    }
}
