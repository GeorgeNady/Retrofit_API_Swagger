package com.github.georgenady.retrofitApiSwagger.presentation.main

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.domain.model.enums.ViewMode

data class MainToolUiState(
    val isLoading: Boolean = false,
    val progressMessage: String? = null,
    val progressFraction: Double = 0.0,
    val allEndpoints: List<ApiNode> = emptyList(),
    val filteredEndpoints: List<ApiNode> = emptyList(),
    val selectedNode: ApiNode? = null,
    val expandedNode: ApiNode? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val totalScanned: Int = 0,
    val currentScanned: Int = 0,
    val totalFilesToScan: Int = 0,
    val durationMs: Long = 0,
    val errorMessage: String? = null,
    val requestResults: Map<String, String> = emptyMap() // Map of method signature to response
)
