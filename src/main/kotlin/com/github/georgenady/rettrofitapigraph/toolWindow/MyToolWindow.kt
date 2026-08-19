package com.github.georgenady.rettrofitapigraph.toolWindow

import com.github.georgenady.rettrofitapigraph.services.RetrofitApiService
import com.github.georgenady.rettrofitapigraph.ui.ApiListPanel
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
    private val listPanel = ApiListPanel() // Updated to ApiListPanel
    private val cardLayout = CardLayout()
    private val contentPanel = JPanel(cardLayout)
    private val mainWrapper = JPanel(BorderLayout())

    init {
        mainWrapper.putClientProperty("MyToolWindow", this)

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

        listPanel.onRefreshRequested = { refresh() }

        contentPanel.add(loadingPanel, "LOADING")
        contentPanel.add(listPanel, "GRAPH")

        mainWrapper.add(toolbarPanel, BorderLayout.NORTH)
        mainWrapper.add(contentPanel, BorderLayout.CENTER)

        refresh()
    }

    fun getComponent() = mainWrapper

    fun refresh() {
        cardLayout.show(contentPanel, "LOADING")

        DumbService.getInstance(project).runWhenSmart {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Retrofit API Discovery", true) {
                override fun run(indicator: ProgressIndicator) {
                    val result = apiService.findRetrofitEndpoints(indicator)

                    ApplicationManager.getApplication().invokeLater {
                        listPanel.setEndpoints(result.endpoints)

                        val status = if (result.isDumb) {
                            "Indexing in progress. Please wait and scan again."
                        } else {
                            "Found ${result.endpoints.size} endpoints in ${result.durationMs}ms."
                        }

                        listPanel.setStatus(status)
                        cardLayout.show(contentPanel, "GRAPH")
                    }
                }

                override fun onCancel() {
                    ApplicationManager.getApplication().invokeLater {
                        listPanel.setStatus("Scan cancelled.")
                        cardLayout.show(contentPanel, "GRAPH")
                    }
                }
            })
        }
    }
}