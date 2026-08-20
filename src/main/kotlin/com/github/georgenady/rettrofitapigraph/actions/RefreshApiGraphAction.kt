package com.github.georgenady.rettrofitapigraph.actions

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.services.ApiStateService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class RefreshApiGraphAction : AnAction(
    MyBundle.message("action.refresh.text"),
    MyBundle.message("action.refresh.description"),
    AllIcons.Actions.Refresh
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        project.service<ApiStateService>().refresh()
    }
}
