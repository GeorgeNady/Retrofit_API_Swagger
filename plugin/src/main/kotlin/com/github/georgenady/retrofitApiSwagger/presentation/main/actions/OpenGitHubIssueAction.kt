package com.github.georgenady.retrofitApiSwagger.presentation.main.actions

import com.github.georgenady.retrofitApiSwagger.MyBundle
import com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel.JcefWebviewAdapter
import com.github.georgenady.retrofitApiSwagger.utils.PluginUiUtils
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenGitHubIssueAction : AnAction(
    MyBundle.message("action.report_issue.text"),
    MyBundle.message("action.report_issue.description"),
    AllIcons.Vcs.Vendors.Github
) {
    override fun actionPerformed(e: AnActionEvent) {
        PluginUiUtils.openBrowser("https://github.com/GeorgeNady/Retrofit_API_Swagger/issues/new")
    }

    override fun update(e: AnActionEvent) {
        // Hidden when the plugin is up and working normally; visible only if issues occur
        e.presentation.isEnabledAndVisible = !JcefWebviewAdapter.isJcefAvailable()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
