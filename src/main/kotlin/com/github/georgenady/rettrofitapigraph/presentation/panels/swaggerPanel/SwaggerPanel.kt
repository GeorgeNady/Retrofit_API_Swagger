package com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.domain.usecase.GroupEndpointsByServiceUseCase
import com.github.georgenady.rettrofitapigraph.presentation.main.MainToolViewModel
import com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components.SwaggerServiceGroup
import com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components.SwaggerApiCard
import com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.SwaggerPanelViewModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class SwaggerPanel(
    private val project: Project,
) : JPanel(BorderLayout()) {

    private val swaggerViewModel = service<SwaggerPanelViewModel>()
    private val groupUseCase = GroupEndpointsByServiceUseCase()
    private var subscriptionJob: Job? = null
    
    private var isScrollModeEnabled = false
    private var lastEndpoints: List<ApiNode>? = null

    private val listPanel = object : JPanel(), javax.swing.Scrollable {
        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(8)
        }

        override fun getScrollableTracksViewportWidth(): Boolean = !isScrollModeEnabled
        override fun getScrollableTracksViewportHeight(): Boolean = false
        override fun getPreferredScrollableViewportSize(): java.awt.Dimension = preferredSize
        override fun getScrollableUnitIncrement(visibleRect: java.awt.Rectangle?, orientation: Int, direction: Int): Int = 10
        override fun getScrollableBlockIncrement(visibleRect: java.awt.Rectangle?, orientation: Int, direction: Int): Int = 50
    }

    private val scrollPane = JBScrollPane(listPanel).apply {
        border = BorderFactory.createEmptyBorder()
        horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    }

    init {
        setupToolbar()
        add(scrollPane, BorderLayout.CENTER)
    }

    private fun setupToolbar() {
        val actionGroup = DefaultActionGroup().apply {
            add(object : ToggleAction(
                MyBundle.message("action.toggle_horizontal_scroll.text"),
                MyBundle.message("action.toggle_horizontal_scroll.description"),
                AllIcons.Actions.ToggleSoftWrap
            ) {
                override fun isSelected(e: AnActionEvent): Boolean = isScrollModeEnabled
                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    swaggerViewModel.toggleScrollMode()
                }
                override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
            })
        }

        val toolbar = ActionManager.getInstance().createActionToolbar("SwaggerPanelToolbar", actionGroup, true).apply {
            targetComponent = this@SwaggerPanel
        }
        add(toolbar.component, BorderLayout.NORTH)
    }

    override fun addNotify() {
        super.addNotify()
        val scope = project.service<MainToolViewModel>().viewModelScope
        subscriptionJob = scope.launch(Dispatchers.Main) {
            swaggerViewModel.uiState.collectLatest { state ->
                isScrollModeEnabled = state.isScrollModeEnabled
                updateScrollPolicy()
            }
        }
    }

    override fun removeNotify() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        super.removeNotify()
    }

    private fun updateScrollPolicy() {
        scrollPane.horizontalScrollBarPolicy = if (isScrollModeEnabled) {
            JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        } else {
            JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }
        listPanel.revalidate()
        listPanel.repaint()
    }

    fun render(
        endpoints: List<ApiNode>
    ) {
        if (lastEndpoints == endpoints) {
            return
        }

        lastEndpoints = endpoints
        listPanel.removeAll()

        val groupedEndpoints = groupUseCase(endpoints)

        for ((className, serviceEndpoints) in groupedEndpoints) {
            val groupPanel = SwaggerServiceGroup(project, className, serviceEndpoints)
            listPanel.add(groupPanel)
            listPanel.add(Box.createVerticalStrut(12))
        }

        listPanel.revalidate()
        listPanel.repaint()
    }
}
