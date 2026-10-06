package com.github.georgenady.retrofitApiSwagger.presentation.main.actions

import com.github.georgenady.retrofitApiSwagger.MyBundle
import com.github.georgenady.retrofitApiSwagger.core.configuration.SwaggerSettingsConfigurable
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

class OpenSwaggerSettingsAction : AnAction(
    MyBundle.message("action.settings.text"),
    MyBundle.message("action.settings.description"),
    AllIcons.General.Settings
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        ShowSettingsUtil.getInstance().showSettingsDialog(
            project,
            SwaggerSettingsConfigurable::class.java
        )
    }
}