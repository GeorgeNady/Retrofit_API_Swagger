package com.github.georgenady.rettrofitapigraph.factory

import com.github.georgenady.rettrofitapigraph.model.ApiEndpoint
import com.github.georgenady.rettrofitapigraph.services.ApiScanner
import com.github.georgenady.rettrofitapigraph.ui.ApiGraphComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

class ApiGraphToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val rootPanel = JPanel(BorderLayout())

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(rootPanel, "API Flow", false)
        toolWindow.contentManager.addContent(content)

        // Add a Refresh Action to the ToolWindow Header toolbar
        toolWindow.setTitleActions(listOf(object : AnAction(
            "Refresh Graph",
            "Rescan project APIs",
            com.intellij.icons.AllIcons.Actions.Refresh
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                scheduleScan(project, rootPanel)
            }
        }))

        // Schedule initial scan when IDE is smart (indexes ready)
        scheduleScan(project, rootPanel)
    }

    private fun scheduleScan(project: Project, container: JPanel) {
        showLoadingState(container)

        // Wait until IDE finishes indexing before running PSI queries
        DumbService.getInstance(project).runWhenSmart {
            // Run expensive PSI parsing asynchronously off the UI thread
            ProgressManager.getInstance()
                .run(object : Task.Backgroundable(project, "Scanning APIs for Graph...", false) {
                    override fun run(indicator: ProgressIndicator) {
                        indicator.isIndeterminate = true

                        // Safely acquire a ReadLock for PSI traversal
                        val endpoints =
                            ReadAction.compute<List<ApiEndpoint>, Exception> {
                                val scanner = ApiScanner(project)
                                scanner.scanForRetrofitApis()
                            }

                        // Render UI on the Event Dispatch Thread (EDT)
                        ApplicationManager.getApplication().invokeLater({
                            renderGraph(project, container, endpoints)
                        }, ModalityState.defaultModalityState())
                    }
                })
        }
    }

    private fun showLoadingState(container: JPanel) {
        container.removeAll()
        val loadingPanel = JPanel(GridBagLayout())
        val progressIcon = AsyncProcessIcon("Loading APIs")
        val label = JBLabel("Indexing & Scanning APIs...", SwingConstants.CENTER)

        val contentBox = JPanel()
        contentBox.add(progressIcon)
        contentBox.add(label)

        loadingPanel.add(contentBox)
        container.add(loadingPanel, BorderLayout.CENTER)
        container.revalidate()
        container.repaint()
    }

    private fun renderGraph(project: Project, container: JPanel, endpoints: List<ApiEndpoint>) {
        container.removeAll()

        // 1. Instantiate the parameterless ApiGraphComponent
        val graphComponent = ApiGraphComponent()

        // 2. Handle in-canvas refresh request (e.g. clicking "SCAN AGAIN")
        graphComponent.onRefreshRequested = {
            scheduleScan(project, container)
        }

        // 3. Populate data into the canvas
        graphComponent.updateData(endpoints)
        graphComponent.setStatus("Found ${endpoints.size} API endpoints across interfaces.")

        // 4. Attach to root container
        container.add(graphComponent, BorderLayout.CENTER)

        container.revalidate()
        container.repaint()
    }
}