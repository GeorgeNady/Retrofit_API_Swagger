package com.github.georgenady.androidapigraph.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.georgenady.androidapigraph.services.RetrofitApiService
import com.github.georgenady.androidapigraph.ui.ApiGraphComponent
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JButton

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val project: Project) {

        private val apiService = project.service<RetrofitApiService>()
        private val graphComponent = ApiGraphComponent()

        fun getContent() = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            add(JButton("Refresh API Graph").apply {
                addActionListener {
                    refresh()
                }
            }, BorderLayout.NORTH)
            add(graphComponent, BorderLayout.CENTER)
            
            // Initial load
            refresh()
        }

        private fun refresh() {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Scanning for Retrofit APIs") {
                override fun run(indicator: ProgressIndicator) {
                    val endpoints = apiService.findRetrofitEndpoints()
                    
                    // Update UI on EDT
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        graphComponent.updateData(endpoints)
                    }
                }
            })
        }
    }
}
