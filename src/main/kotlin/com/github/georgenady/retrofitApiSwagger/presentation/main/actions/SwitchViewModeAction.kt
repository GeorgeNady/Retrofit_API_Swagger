package com.github.georgenady.retrofitApiSwagger.presentation.main.actions

import com.github.georgenady.retrofitApiSwagger.MyBundle
import com.github.georgenady.retrofitApiSwagger.domain.model.enums.ViewMode
import com.github.georgenady.retrofitApiSwagger.presentation.main.MainToolViewModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.service

class SwitchViewModeAction : AnAction(
    MyBundle.message("action.switch_view.text"),
    MyBundle.message("action.switch_view.description"),
    AllIcons.Actions.Diff
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val viewModel = project.service<MainToolViewModel>()
        val nextMode = when (viewModel.uiState.value.viewMode) {
            ViewMode.LIST -> ViewMode.GRAPH
            ViewMode.GRAPH -> ViewMode.LIST
        }
        viewModel.setViewMode(nextMode)
    }
}
