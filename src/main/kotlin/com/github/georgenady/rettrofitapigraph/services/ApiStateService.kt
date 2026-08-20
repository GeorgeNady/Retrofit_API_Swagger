package com.github.georgenady.rettrofitapigraph.services

import com.github.georgenady.rettrofitapigraph.model.ApiFilterModel
import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

@Service(Service.Level.PROJECT)
class ApiStateService(private val project: Project) {

    enum class ViewMode { DUAL, LIST, GRAPH }

    interface ApiStateListener {
        fun onEndpointsUpdated(endpoints: List<ApiNode>, totalScanned: Int, durationMs: Long) {}
        fun onFilteredEndpointsUpdated(filtered: List<ApiNode>) {}
        fun onNodeSelected(node: ApiNode?) {}
        fun onLoadingStateChanged(isLoading: Boolean) {}
        fun onViewModeChanged(mode: ViewMode) {}
    }

    companion object {
        val TOPIC = Topic.create("ApiStateChanged", ApiStateListener::class.java)
    }

    private var allEndpoints: List<ApiNode> = emptyList()
    private var filteredEndpoints: List<ApiNode> = emptyList()
    private var selectedNode: ApiNode? = null
    private var currentFilter = ApiFilterModel()
    private var currentViewMode = ViewMode.DUAL

    fun refresh() {
        DumbService.getInstance(project).runWhenSmart {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Retrofit API Discovery", true) {
                override fun run(indicator: ProgressIndicator) {
                    publishLoading(true)
                    val apiService = project.service<RetrofitApiService>()
                    val result = apiService.findRetrofitEndpoints(indicator)
                    
                    ApplicationManager.getApplication().invokeLater {
                        allEndpoints = result.endpoints
                        applyFilters()
                        project.messageBus.syncPublisher(TOPIC).onEndpointsUpdated(allEndpoints, result.filesScanned, result.durationMs)
                        publishLoading(false)
                    }
                }

                override fun onCancel() {
                    publishLoading(false)
                }
            })
        }
    }

    fun setViewMode(mode: ViewMode) {
        currentViewMode = mode
        project.messageBus.syncPublisher(TOPIC).onViewModeChanged(mode)
    }

    fun getViewMode() = currentViewMode

    fun setFilter(filter: ApiFilterModel) {
        currentFilter = filter
        applyFilters()
    }

    fun selectNode(node: ApiNode?) {
        selectedNode = node
        project.messageBus.syncPublisher(TOPIC).onNodeSelected(node)
    }

    fun getSelectedNode() = selectedNode
    fun getFilteredEndpoints() = filteredEndpoints
    fun getAllEndpoints() = allEndpoints

    private fun applyFilters() {
        filteredEndpoints = allEndpoints.filter { currentFilter.matches(it) }
        project.messageBus.syncPublisher(TOPIC).onFilteredEndpointsUpdated(filteredEndpoints)
    }

    private fun publishLoading(isLoading: Boolean) {
        ApplicationManager.getApplication().invokeLater {
            project.messageBus.syncPublisher(TOPIC).onLoadingStateChanged(isLoading)
        }
    }
}
