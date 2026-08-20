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
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.SwingConstants

class ApiMainDashboard(private val project: Project) : JPanel(BorderLayout()) {

    private val stateService = project.service<ApiStateService>()
    
    private val cardLayout = CardLayout()
    private val contentSwitcher = JPanel(cardLayout)
    private val statusBar = ApiStatusBarView()

    // UI Sections
    private val filterSection = FilterSection { newFilter ->
        stateService.setFilter(newFilter)
    }
    private val detailsSection = DetailsSection()

    // 1. API List Panel
    private val listPanel = ApiCardListContainer { selectedNode ->
        stateService.selectNode(selectedNode)
    }

    // 2. API Graph Panel
    private val graphPanel = ApiGraphPanel(project) { selectedNode ->
        stateService.selectNode(selectedNode)
    }

    // 3. Tools Side Panel (Set a reasonable preferred width constraint)
    private val sidePanel = FeatureSidePanel(project).apply {
        addSection(filterSection)
        addSection(detailsSection)

        // FIX 1: Allow side panel to collapse as small as 150px without pushing splitters
        minimumSize = Dimension(JBUI.scale(150), 0)
        preferredSize = Dimension(JBUI.scale(260), preferredSize.height)
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

    // Enable minimum size honors on OnePixelSplitter
    private val leftSplitter = OnePixelSplitter(false, 0.4f).apply {
        firstComponent = listPanel
        secondComponent = graphPanel
        setHonorComponentsMinimumSize(true)
    }

    private val mainSplitter = object : OnePixelSplitter(false, 1.0f) {
        override fun setProportion(proportion: Float) {
            val clamped = proportion.coerceIn(0.50f, 0.95f)
            super.setProportion(clamped)
        }
    }.apply {
        firstComponent = leftSplitter
        secondComponent = null // Hidden by default
        setHonorComponentsMinimumSize(true)
    }

    init {
        mainWrapperPutClientProperty()

        contentSwitcher.add(mainSplitter, "MAIN")
        contentSwitcher.add(emptyStateView, "EMPTY")
        contentSwitcher.add(loadingPanel, "LOADING")

        add(contentSwitcher, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)

        setupSubscriptions()
        
        // Set initial view mode based on service state
        updateViewMode(stateService.getViewMode())
    }

    private fun mainWrapperPutClientProperty() {
        putClientProperty("ApiMainDashboard", this)
    }

    private fun setupSubscriptions() {
        project.messageBus.connect().subscribe(ApiStateService.TOPIC, object : ApiStateService.ApiStateListener {
            override fun onEndpointsUpdated(endpoints: List<ApiNode>, totalScanned: Int, durationMs: Long) {
                statusBar.setMessage("🔎 Found ${endpoints.size} endpoints in $durationMs ms.")
                
                // Update module list in filter section
                val modules = endpoints.map { it.className }.distinct().sorted()
                filterSection.updateModules(modules)
            }

            override fun onFilteredEndpointsUpdated(filtered: List<ApiNode>) {
                renderData(filtered)
            }

            override fun onNodeSelected(node: ApiNode?) {
                detailsSection.onNodeSelected(node)
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

    private fun renderData(filteredEndpoints: List<ApiNode>) {
        val allEndpoints = stateService.getAllEndpoints()

        if (allEndpoints.isEmpty()) {
            cardLayout.show(contentSwitcher, "EMPTY")
            return
        }

        if (filteredEndpoints.isEmpty()) {
            // Revert to all APIs if the filter matched nothing
            listPanel.render(allEndpoints)
            graphPanel.render(allEndpoints)
            statusBar.setMessage("No matches found. Showing all APIs.")
        } else {
            listPanel.render(filteredEndpoints)
            graphPanel.render(filteredEndpoints)
        }

        cardLayout.show(contentSwitcher, "MAIN")
    }

    fun toggleSidePanel() {
        if (mainSplitter.secondComponent == null) {
            mainSplitter.secondComponent = sidePanel
            mainSplitter.proportion = 0.75f
        } else {
            mainSplitter.secondComponent = null
            mainSplitter.proportion = 1.0f
        }
        mainSplitter.revalidate()
        mainSplitter.repaint()
    }
}