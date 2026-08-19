package com.github.georgenady.rettrofitapigraph.toolWindow

import com.github.georgenady.rettrofitapigraph.services.RetrofitApiService
import com.github.georgenady.rettrofitapigraph.ui.ApiGraphComponent
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class MyToolWindow(private val project: Project) {

    private val apiService = project.service<RetrofitApiService>()
    private val graphComponent = ApiGraphComponent(project)
    private val cardLayout = CardLayout()
    private val contentPanel = JPanel(cardLayout)
    private val mainWrapper = JPanel(BorderLayout())

    init {
        val toolbarPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        toolbarPanel.add(JButton("Scan Project", AllIcons.Actions.Find).apply {
            addActionListener { refresh() }
        })

        val loadingPanel = JPanel(BorderLayout()).apply {
            val centerPanel = JPanel(BorderLayout())
            centerPanel.add(AsyncProcessIcon("Scanning"), BorderLayout.NORTH)
            centerPanel.add(
                JBLabel("Discovering API Endpoints...", SwingConstants.CENTER),
                BorderLayout.SOUTH
            )
            add(centerPanel, BorderLayout.CENTER)
        }

        graphComponent.onRefreshRequested = { refresh() }

        contentPanel.add(loadingPanel, "LOADING")
        contentPanel.add(graphComponent, "GRAPH")

        mainWrapper.add(toolbarPanel, BorderLayout.NORTH)
        mainWrapper.add(contentPanel, BorderLayout.CENTER)

        refresh()
    }

    fun getComponent() = mainWrapper

    fun refresh() {
        cardLayout.show(contentPanel, "LOADING")

        DumbService.getInstance(project).runWhenSmart {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Retrofit API Discovery") {
                override fun run(indicator: ProgressIndicator) {
                    val result = apiService.findRetrofitEndpoints()

                    ApplicationManager.getApplication().invokeLater {
                        graphComponent.updateData(result.endpoints)

                        val status = if (result.isDumb) {
                            "Indexing in progress. Please wait and scan again."
                        } else {
                            "Found ${result.endpoints.size} endpoints in ${result.durationMs}ms."
                        }

                        graphComponent.setStatus(status)
                        cardLayout.show(contentPanel, "GRAPH")
                    }
                }
            })
        }
    }
}