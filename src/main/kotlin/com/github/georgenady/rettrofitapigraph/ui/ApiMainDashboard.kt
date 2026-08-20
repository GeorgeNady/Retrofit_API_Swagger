package com.github.georgenady.rettrofitapigraph.ui

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.services.ApiStateService
import com.github.georgenady.rettrofitapigraph.ui.components.ApiCardListContainer
import com.github.georgenady.rettrofitapigraph.ui.components.ApiEmptyStateView
import com.github.georgenady.rettrofitapigraph.ui.components.ApiStatusBarView
import com.github.georgenady.rettrofitapigraph.ui.graph.ApiGraphPanel
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.FeatureSidePanel
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.sections.DetailsSection
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.sections.FilterSection
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

class ApiMainDashboard(private val project: Project) : JPanel(BorderLayout()) {

    private val stateService = project.service<ApiStateService>()
    
    private val cardLayout = CardLayout()
    private val contentSwitcher = JPanel(cardLayout)
    private val statusBar = ApiStatusBarView()

    // 1. API List Panel
    private val listPanel = ApiCardListContainer { selectedNode ->
        stateService.selectNode(selectedNode)
    }

    // 2. API Graph Panel
    private val graphPanel = ApiGraphPanel(project) { selectedNode ->
        stateService.selectNode(selectedNode)
    }

    // 3. Tools Side Panel
    private val sidePanel = FeatureSidePanel(project).apply {
        val filterSection = FilterSection { newFilter ->
            stateService.setFilter(newFilter)
        }
        addSection(filterSection)
        addSection(DetailsSection())
    }

    private val emptyStateView = ApiEmptyStateView {
        stateService.refresh()
    }

    private val loadingPanel = JPanel(BorderLayout()).apply {
        isOpaque = false
        val centerPanel = JPanel(BorderLayout()).apply { isOpaque = false }
        centerPanel.add(AsyncProcessIcon("Scanning"), BorderLayout.NORTH)
        centerPanel.add(JBLabel("Discovering APIs...", SwingConstants.CENTER), BorderLayout.SOUTH)
        add(centerPanel, BorderLayout.CENTER)
    }

    // Layout Splitters
    private val leftSplitter = OnePixelSplitter(false, 0.4f).apply {
        firstComponent = listPanel
        secondComponent = graphPanel
    }

    private val mainSplitter = OnePixelSplitter(false, 0.8f).apply {
        firstComponent = leftSplitter
        secondComponent = sidePanel
    }

    init {
        mainWrapperPutClientProperty()

        contentSwitcher.add(mainSplitter, "MAIN")
        contentSwitcher.add(emptyStateView, "EMPTY")
        contentSwitcher.add(loadingPanel, "LOADING")

        add(contentSwitcher, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)

        setupSubscriptions()
    }

    private fun mainWrapperPutClientProperty() {
        putClientProperty("ApiMainDashboard", this)
    }

    private fun setupSubscriptions() {
        project.messageBus.connect().subscribe(ApiStateService.TOPIC, object : ApiStateService.ApiStateListener {
            override fun onEndpointsUpdated(endpoints: List<ApiNode>, totalScanned: Int, durationMs: Long) {
                statusBar.setMessage("🔎 Found ${endpoints.size}, 🛜 endpoints, ⌛ in $durationMs ms.")
            }

            override fun onFilteredEndpointsUpdated(filtered: List<ApiNode>) {
                renderData(filtered)
            }

            override fun onLoadingStateChanged(isLoading: Boolean) {
                if (isLoading) {
                    cardLayout.show(contentSwitcher, "LOADING")
                }
            }

            override fun onViewModeChanged(mode: ApiStateService.ViewMode) {
                updateViewMode(mode)
            }
        })
    }

    private fun updateViewMode(mode: ApiStateService.ViewMode) {
        when (mode) {
            ApiStateService.ViewMode.DUAL -> {
                leftSplitter.firstComponent = listPanel
                leftSplitter.secondComponent = graphPanel
                leftSplitter.proportion = 0.4f
            }
            ApiStateService.ViewMode.LIST -> {
                leftSplitter.firstComponent = listPanel
                leftSplitter.secondComponent = null
                leftSplitter.proportion = 1.0f
            }
            ApiStateService.ViewMode.GRAPH -> {
                leftSplitter.firstComponent = null
                leftSplitter.secondComponent = graphPanel
                leftSplitter.proportion = 0.0f
            }
        }
        leftSplitter.revalidate()
        leftSplitter.repaint()
    }

    private fun renderData(endpoints: List<ApiNode>) {
        if (endpoints.isEmpty()) {
            cardLayout.show(contentSwitcher, "EMPTY")
            return
        }

        listPanel.render(endpoints)
        graphPanel.render(endpoints)
        cardLayout.show(contentSwitcher, "MAIN")
    }

    fun toggleSidePanel() {
        if (mainSplitter.secondComponent == null) {
            mainSplitter.secondComponent = sidePanel
            mainSplitter.proportion = 0.8f
        } else {
            mainSplitter.secondComponent = null
            mainSplitter.proportion = 1.0f
        }
        mainSplitter.revalidate()
        mainSplitter.repaint()
    }
}
