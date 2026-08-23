package com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel

import com.intellij.openapi.components.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Service(Service.Level.APP)
class SwaggerPanelViewModel {
    private val _uiState = MutableStateFlow(SwaggerPanelUiState())
    val uiState: StateFlow<SwaggerPanelUiState> = _uiState.asStateFlow()

    fun toggleScrollMode() {
        _uiState.update { it.copy(isScrollModeEnabled = !it.isScrollModeEnabled) }
    }
}
