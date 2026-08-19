package com.github.georgenady.rettrofitapigraph.toolWindow

import com.github.georgenady.rettrofitapigraph.model.ApiFilterModel
import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.services.RetrofitApiService
import com.github.georgenady.rettrofitapigraph.ui.ApiListPanel
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.FeatureSidePanel
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.sections.DetailsSection
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.sections.FilterSection
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

class MyToolWindow(private val project: Project) {

    private val apiService = project.service<RetrofitApiService>()
    private val listPanel = ApiListPanel()
    private val sidePanel = FeatureSidePanel()
    private val cardLayout = CardLayout()
    private val contentPanel = JPanel(cardLayout)
    private val mainWrapper = JPanel(BorderLayout())

    private var allEndpoints: List<ApiNode> = emptyList()
    private var currentFilter = ApiFilterModel()

    private val splitter = OnePixelSplitter(false, 1.0f).apply {
        firstComponent = contentPanel
        secondComponent = null // Hidden by default
    }

    private val filterSection = FilterSection { newFilter ->
        currentFilter = newFilter
        applyFilters()
    }

    init {
        mainWrapper.putClientProperty("MyToolWindow", this)

        val loadingPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            val centerPanel = JPanel(BorderLayout()).apply { isOpaque = false }
            centerPanel.add(AsyncProcessIcon("Scanning"), BorderLayout.NORTH)
            centerPanel.add(
                JBLabel("Discovering API Endpoints...", SwingConstants.CENTER),
                BorderLayout.SOUTH
            )
            add(centerPanel, BorderLayout.CENTER)
        }

        listPanel.onRefreshRequested = { refresh() }
        listPanel.onNodeSelected = { node ->
            sidePanel.notifyNodeSelected(node)
        }

        sidePanel.addSection(filterSection)
        sidePanel.addSection(DetailsSection())

        contentPanel.add(loadingPanel, "LOADING")
        contentPanel.add(listPanel, "GRAPH")

        // Splitter is already horizontal (vertical = false)
        mainWrapper.add(splitter, BorderLayout.CENTER)

        refresh()
    }

    fun getComponent() = mainWrapper

    fun toggleSidePanel() {
        if (splitter.secondComponent == null) {
            splitter.secondComponent = sidePanel
            splitter.proportion = 0.7f
        } else {
            splitter.secondComponent = null
            splitter.proportion = 1.0f
        }
        splitter.revalidate()
        splitter.repaint()
    }

    private fun applyFilters() {
        val filtered = allEndpoints.filter { currentFilter.matches(it) }
        listPanel.setEndpoints(filtered)
        listPanel.setStatus("Showing ${filtered.size} of ${allEndpoints.size} endpoints.")
    }

    fun refresh() {
        cardLayout.show(contentPanel, "LOADING")

        DumbService.getInstance(project).runWhenSmart {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Retrofit API Discovery", true) {
                override fun run(indicator: ProgressIndicator) {
                    val result = apiService.findRetrofitEndpoints(indicator)

                    ApplicationManager.getApplication().invokeLater {
                        allEndpoints = result.endpoints
                        
                        val modules = allEndpoints.map { it.className }.distinct().sorted()
                        filterSection.updateModules(modules)
                        
                        applyFilters()

                        if (result.isDumb) {
                            listPanel.setStatus("Indexing in progress. Please wait and scan again.")
                        }
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
