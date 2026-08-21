package com.github.georgenady.rettrofitapigraph.presentation.viewmodel

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.viewmodel.ApiDashboardViewModel.ViewMode

data class ApiDashboardUiState(
    val isLoading: Boolean = false,
    val progressMessage: String? = null,
    val progressFraction: Double = 0.0,
    val allEndpoints: List<ApiNode> = emptyList(),
    val filteredEndpoints: List<ApiNode> = emptyList(),
    val selectedNode: ApiNode? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val totalScanned: Int = 0,
    val durationMs: Long = 0,
    val errorMessage: String? = null
)
