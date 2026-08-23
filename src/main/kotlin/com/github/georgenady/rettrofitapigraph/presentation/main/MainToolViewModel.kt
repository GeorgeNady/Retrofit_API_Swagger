package com.github.georgenady.rettrofitapigraph.presentation.main

import com.github.georgenady.rettrofitapigraph.domain.model.ApiFilterModel
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.domain.model.ScanOperation
import com.github.georgenady.rettrofitapigraph.domain.model.enums.ViewMode
import com.github.georgenady.rettrofitapigraph.domain.repository.ApiRepository
import com.github.georgenady.rettrofitapigraph.domain.usecase.FilterEndpointsUseCase
import com.github.georgenady.rettrofitapigraph.domain.usecase.ScanProjectEndpointsUseCase
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class MainToolViewModel(
    private val project: Project,
    val viewModelScope: CoroutineScope
) {

    private val repository = project.service<ApiRepository>()
    private val scanProjectUseCase = ScanProjectEndpointsUseCase(repository)
    private val filterUseCase = FilterEndpointsUseCase()

    private val _uiState = MutableStateFlow(MainToolUiState())
    val uiState: StateFlow<MainToolUiState> = _uiState.asStateFlow()

    private var currentFilter = ApiFilterModel()
    private var scanJob: Job? = null

    fun refresh() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            scanProjectUseCase()
                .catch { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message) }
                }
                .onCompletion {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { op ->
                    when (op) {
                        is ScanOperation.Started -> {
                            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                        }
                        is ScanOperation.InProgress -> {
                            _uiState.update { 
                                it.copy(
                                    progressFraction = op.fraction,
                                    progressMessage = op.currentFile
                                )
                            }
                        }
                        is ScanOperation.Completed -> {
                            _uiState.update { 
                                it.copy(
                                    allEndpoints = op.result.endpoints,
                                    totalScanned = op.result.filesScanned,
                                    durationMs = op.result.durationMs,
                                    filteredEndpoints = filterUseCase(op.result.endpoints, currentFilter)
                                )
                            }
                        }
                        is ScanOperation.Failed -> {
                            _uiState.update { it.copy(errorMessage = op.throwable.message) }
                        }
                    }
                }
        }
    }

    fun setFilter(filter: ApiFilterModel) {
        currentFilter = filter
        _uiState.update { 
            it.copy(filteredEndpoints = filterUseCase(it.allEndpoints, currentFilter))
        }
    }

    fun selectNode(node: ApiNode?) {
        _uiState.update { it.copy(selectedNode = node) }
    }

    fun toggleExpansion(node: ApiNode) {
        _uiState.update {
            val newExpanded = if (it.expandedNode == node) null else node
            it.copy(expandedNode = newExpanded)
        }
    }

    fun expandNode(node: ApiNode) {
        _uiState.update { it.copy(expandedNode = node) }
    }

    fun clearExpansion() {
        _uiState.update { it.copy(expandedNode = null) }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun executeApiCall(node: ApiNode, url: String, body: String?) {
        viewModelScope.launch {
            val executeUseCase = com.github.georgenady.rettrofitapigraph.domain.usecase.ExecuteHttpRequestUseCase()
            val settings = com.github.georgenady.rettrofitapigraph.data.service.SwaggerSettingsService.getInstance()
            
            val result = executeUseCase.invoke(
                url = url,
                method = node.httpMethod,
                headers = settings.state.defaultHeaders,
                body = body
            )

            val responseText = result.getOrElse { "Error: ${it.message}" }
            
            _uiState.update { state ->
                val newResults = state.requestResults.toMutableMap()
                newResults["${node.className}.${node.methodName}"] = responseText
                state.copy(requestResults = newResults)
            }
        }
    }
}
