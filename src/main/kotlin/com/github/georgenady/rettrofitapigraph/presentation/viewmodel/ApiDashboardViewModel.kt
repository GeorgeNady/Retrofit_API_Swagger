package com.github.georgenady.rettrofitapigraph.presentation.viewmodel

import com.github.georgenady.rettrofitapigraph.domain.model.ApiFilterModel
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.domain.model.ScanOperation
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
class ApiDashboardViewModel(
    private val project: Project,
    val cs: CoroutineScope
) {
    enum class ViewMode { LIST, GRAPH }

    private val repository = project.service<ApiRepository>()
    private val scanProjectUseCase = ScanProjectEndpointsUseCase(repository)
    private val filterUseCase = FilterEndpointsUseCase()

    private val _uiState = MutableStateFlow(ApiDashboardUiState())
    val uiState: StateFlow<ApiDashboardUiState> = _uiState.asStateFlow()

    private var currentFilter = ApiFilterModel()
    private var scanJob: Job? = null

    fun refresh() {
        scanJob?.cancel()
        scanJob = cs.launch {
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

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }
}
