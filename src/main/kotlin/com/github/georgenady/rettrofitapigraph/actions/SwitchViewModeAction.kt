package com.github.georgenady.rettrofitapigraph.actions

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.services.ApiStateService
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
        val stateService = project.service<ApiStateService>()
        val nextMode = when (stateService.getViewMode()) {
            ApiStateService.ViewMode.LIST -> ApiStateService.ViewMode.GRAPH
            ApiStateService.ViewMode.GRAPH -> ApiStateService.ViewMode.LIST
        }
        stateService.setViewMode(nextMode)
    }
}
