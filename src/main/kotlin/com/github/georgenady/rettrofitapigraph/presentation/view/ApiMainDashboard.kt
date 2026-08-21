package com.github.georgenady.rettrofitapigraph.presentation.view

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.presentation.viewmodel.ApiDashboardViewModel
import com.github.georgenady.rettrofitapigraph.presentation.viewmodel.ApiDashboardUiState
import com.github.georgenady.rettrofitapigraph.presentation.components.ApiCardListContainer
import com.github.georgenady.rettrofitapigraph.presentation.components.ApiEmptyStateView
import com.github.georgenady.rettrofitapigraph.presentation.components.ApiStatusBarView
import com.github.georgenady.rettrofitapigraph.presentation.graph.ApiGraphPanel
import com.github.georgenady.rettrofitapigraph.presentation.sidepanel.FeatureSidePanel
import com.github.georgenady.rettrofitapigraph.presentation.sidepanel.sections.DetailsSection
import com.github.georgenady.rettrofitapigraph.presentation.sidepanel.sections.FilterSection
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.AsyncProcessIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.SwingConstants

class ApiMainDashboard(private val project: Project) : JPanel(BorderLayout()) {

    private val viewModel = project.service<ApiDashboardViewModel>()
    private var subscriptionJob: Job? = null
    
    private val cardLayout = CardLayout()
    private val contentSwitcher = JPanel(cardLayout)
    private val statusBar = ApiStatusBarView()

    // UI Sections
    private val filterSection = FilterSection { newFilter ->
        viewModel.setFilter(newFilter)
    }
    private val detailsSection = DetailsSection()

    // 1. API List Panel
    private val listPanel = ApiCardListContainer { selectedNode ->
        viewModel.selectNode(selectedNode)
    }

    // 2. API Graph Panel
    private val graphPanel = ApiGraphPanel(project) { selectedNode ->
        viewModel.selectNode(selectedNode)
    }

    // 3. Tools Side Panel
    private val sidePanel = FeatureSidePanel(project).apply {
        addSection(filterSection)
        addSection(detailsSection)
        minimumSize = Dimension(JBUI.scale(150), 0)
        preferredSize = Dimension(JBUI.scale(260), preferredSize.height)
    }

    private val emptyStateView = ApiEmptyStateView {
        viewModel.refresh()
    }

    private val loadingPanel = JPanel(BorderLayout()).apply {
        isOpaque = false
        val centerPanel = JPanel(BorderLayout()).apply { isOpaque = false }
        centerPanel.add(AsyncProcessIcon("Scanning"), BorderLayout.NORTH)
        centerPanel.add(JBLabel(MyBundle.message("dashboard.scanning"), SwingConstants.CENTER), BorderLayout.SOUTH)
        add(centerPanel, BorderLayout.CENTER)
    }

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
        secondComponent = null
        setHonorComponentsMinimumSize(true)
    }

    init {
        putClientProperty("ApiMainDashboard", this)

        contentSwitcher.add(mainSplitter, "MAIN")
        contentSwitcher.add(emptyStateView, "EMPTY")
        contentSwitcher.add(loadingPanel, "LOADING")

        add(contentSwitcher, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)

        // Show loading by default since an initial scan is triggered
        cardLayout.show(contentSwitcher, "LOADING")
    }

    override fun addNotify() {
        super.addNotify()
        // Use the scope from the ViewModel
        subscriptionJob = viewModel.cs.launch(Dispatchers.Main) {
            viewModel.uiState.collectLatest { state ->
                updateUi(state)
            }
        }
    }

    override fun removeNotify() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        super.removeNotify()
    }

    private fun updateUi(state: ApiDashboardUiState) {
        // Update Status Bar
        if (state.totalScanned > 0) {
            statusBar.setMessage(MyBundle.message("dashboard.found_endpoints", state.allEndpoints.size, state.durationMs))
        } else if (state.errorMessage != null) {
            statusBar.setMessage("Error: ${state.errorMessage}")
        }

        // Update Modules in Filter Section
        val modules = state.allEndpoints.map { it.className }.distinct().sorted()
        filterSection.updateModules(modules)

        // Update Details
        detailsSection.onNodeSelected(state.selectedNode)

        // Update View Mode
        updateViewMode(state.viewMode)

        // Render Data
        if (state.isLoading) {
            cardLayout.show(contentSwitcher, "LOADING")
        } else if (state.allEndpoints.isEmpty()) {
            cardLayout.show(contentSwitcher, "EMPTY")
        } else {
            if (state.filteredEndpoints.isEmpty()) {
                listPanel.render(state.allEndpoints)
                graphPanel.render(state.allEndpoints)
                statusBar.setMessage(MyBundle.message("dashboard.no_matches"))
            } else {
                listPanel.render(state.filteredEndpoints)
                graphPanel.render(state.filteredEndpoints)
            }
            cardLayout.show(contentSwitcher, "MAIN")
        }
        
        revalidate()
        repaint()
    }

    private fun updateViewMode(mode: ApiDashboardViewModel.ViewMode) {
        when (mode) {
            ApiDashboardViewModel.ViewMode.LIST -> {
                leftSplitter.firstComponent = listPanel
                leftSplitter.secondComponent = null
                leftSplitter.proportion = 1.0f
            }
            ApiDashboardViewModel.ViewMode.GRAPH -> {
                leftSplitter.firstComponent = null
                leftSplitter.secondComponent = graphPanel
                leftSplitter.proportion = 0.0f
            }
        }
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
