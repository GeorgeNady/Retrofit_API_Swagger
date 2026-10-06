package com.github.georgenady.retrofitApiSwagger.presentation.main

import com.github.georgenady.retrofitApiSwagger.MyBundle
import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.domain.model.enums.ViewMode
import com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel.SwaggerPanel
import com.github.georgenady.retrofitApiSwagger.presentation.components.ApiEmptyStateView
import com.github.georgenady.retrofitApiSwagger.presentation.components.ApiStatusBarView
import com.github.georgenady.retrofitApiSwagger.presentation.components.LoadingView
import com.github.georgenady.retrofitApiSwagger.presentation.panels.sidePanel.FeatureSidePanel
import com.github.georgenady.retrofitApiSwagger.presentation.panels.sidePanel.sections.DetailsSection
import com.github.georgenady.retrofitApiSwagger.presentation.panels.sidePanel.sections.FilterSection
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import javax.swing.JPanel

class MainToolWindow(private val project: Project) : JPanel(BorderLayout()) {

    private val viewModel = project.service<MainToolViewModel>()
    private var subscriptionJob: Job? = null

    private val cardLayout = CardLayout()
    private val contentSwitcher = JPanel(cardLayout)
    private val statusBar = ApiStatusBarView()

    // UI Sections
    private val filterSection = FilterSection { newFilter ->
        viewModel.setFilter(newFilter)
    }
    private val detailsSection = DetailsSection()

    // Unified React Webview Panel (contains both Graph and List views)
    private val mainPanel = SwaggerPanel(
        project = project,
        isEditorMode = false,
        initialViewMode = "graph"
    )

    // Tools Side Panel
    private val sidePanel = FeatureSidePanel(project).apply {
        addSection(filterSection)
        addSection(detailsSection)
        minimumSize = Dimension(JBUI.scale(150), 0)
        preferredSize = Dimension(JBUI.scale(260), preferredSize.height)
    }

    private val emptyStateView = ApiEmptyStateView {
        viewModel.refresh()
    }

    private val loadingPanel = LoadingView()

    private val mainSplitter = object : OnePixelSplitter(false, 1.0f) {
        override fun setProportion(proportion: Float) {
            val clamped = proportion.coerceIn(0.50f, 0.95f)
            super.setProportion(clamped)
        }
    }.apply {
        firstComponent = mainPanel
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
        subscriptionJob = viewModel.viewModelScope.launch(Dispatchers.Main) {
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

    private var lastRenderedEndpoints: List<ApiNode>? = null
    private var lastSelectedNode: ApiNode? = null

    private fun updateUi(state: MainToolUiState) {
        // Update Status Bar
        if (state.totalScanned > 0) {
            statusBar.setMessage(
                MyBundle.message(
                    "dashboard.found_endpoints",
                    state.allEndpoints.size,
                    state.durationMs
                )
            )
        } else if (state.errorMessage != null) {
            statusBar.setMessage("Error: ${state.errorMessage}")
        }

        // Update Modules in Filter Section
        if (lastRenderedEndpoints != state.allEndpoints) {
            val modules = state.allEndpoints.map { it.className }.distinct().sorted()
            filterSection.updateModules(modules)
        }

        // Update Details (only if selection changed)
        if (lastSelectedNode != state.selectedNode) {
            detailsSection.onNodeSelected(state.selectedNode)
            lastSelectedNode = state.selectedNode
        }

        // Update View Mode
        updateViewMode(state.viewMode)

        // Render Data
        if (state.isLoading) {
            cardLayout.show(contentSwitcher, "LOADING")
            val msg = when {
                state.totalFilesToScan > 0 -> "Scanning: ${state.currentScanned}/${state.totalFilesToScan} files..."
                !state.progressMessage.isNullOrBlank() -> state.progressMessage
                else -> MyBundle.message("dashboard.scanning")
            }
            statusBar.setMessage(msg)
            loadingPanel.setMessage(msg)
        } else if (!mainPanel.isJcefAvailable()) {
            // When JCEF is missing, always show MAIN so the missing requirement panel with CTA buttons is visible
            cardLayout.show(contentSwitcher, "MAIN")
        } else if (state.allEndpoints.isEmpty()) {
            cardLayout.show(contentSwitcher, "EMPTY")
        } else {
            val toRender = state.filteredEndpoints.ifEmpty { state.allEndpoints }
            val endpointsChanged = lastRenderedEndpoints != toRender

            // ONLY RENDER IF ENDPOINTS CHANGED
            if (endpointsChanged) {
                mainPanel.render(toRender)
                lastRenderedEndpoints = toRender
            }

            if (state.filteredEndpoints.isEmpty() && state.allEndpoints.isNotEmpty()) {
                statusBar.setMessage(MyBundle.message("dashboard.no_matches"))
            }
            
            cardLayout.show(contentSwitcher, "MAIN")
        }

        revalidate()
        repaint()
    }

    private fun updateViewMode(mode: ViewMode) {
        mainPanel.setViewMode(if (mode == ViewMode.GRAPH) "graph" else "list")
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
