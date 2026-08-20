package com.github.georgenady.rettrofitapigraph.actions

import com.github.georgenady.rettrofitapigraph.services.ApiStateService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.service

class SwitchViewModeAction : AnAction("Switch View Mode", "Toggle between List and Graph views", AllIcons.Actions.Diff) {
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
