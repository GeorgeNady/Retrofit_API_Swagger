package com.github.georgenady.retrofitApiSwagger.presentation.main.actions

import com.github.georgenady.retrofitApiSwagger.MyBundle
import com.github.georgenady.retrofitApiSwagger.domain.model.enums.ViewMode
import com.github.georgenady.retrofitApiSwagger.presentation.main.MainToolViewModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware

class SwitchViewModeAction : AnAction(
    MyBundle.message("action.switch_view.text"),
    MyBundle.message("action.switch_view.description"),
    AllIcons.Actions.Diff
), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project ?: run {
            e.presentation.isEnabled = false
            return
        }
        val viewModel = project.service<MainToolViewModel>()
        val currentMode = viewModel.uiState.value.viewMode
        if (currentMode == ViewMode.GRAPH) {
            e.presentation.text = "Switch to List View"
            e.presentation.description = "Switch to card list view"
            e.presentation.icon = AllIcons.Actions.ListFiles
        } else {
            e.presentation.text = "Switch to Graph View"
            e.presentation.description = "Switch to interactive node graph view"
            e.presentation.icon = AllIcons.Actions.Diff
        }
        e.presentation.isEnabled = true
    }

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
