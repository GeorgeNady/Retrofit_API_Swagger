package com.github.georgenady.retrofitApiSwagger.presentation.main

import com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel.JcefWebviewAdapter
import com.github.georgenady.retrofitApiSwagger.utils.PluginUiUtils
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class MainToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        try {
            val dashboard = MainToolWindow(project)
            val content = ContentFactory.getInstance().createContent(dashboard, null, false)
            toolWindow.contentManager.addContent(content)

            // Force validation to ensure visibility
            dashboard.revalidate()
            dashboard.repaint()

            // Add actions to tool window header
            val actionManager = ActionManager.getInstance()
            val refreshAction = actionManager.getAction("RetrofitAPISwagger.Refresh")
            val toggleAction = actionManager.getAction("RetrofitAPISwagger.ToggleSidePanel")
            val pluginSettings = actionManager.getAction("RetrofitAPISwagger.Settings")
            val gitHubIssue = actionManager.getAction("RetrofitAPISwagger.GitHubIssue")

            val actions = mutableListOf<AnAction>()
            refreshAction?.let { actions.add(it) }
            toggleAction?.let { actions.add(it) }
            pluginSettings?.let { actions.add(it) }
            // Only show GitHub Issue report button if the plugin encounters issues (e.g. JCEF missing)
            if (!JcefWebviewAdapter.isJcefAvailable()) {
                gitHubIssue?.let { actions.add(it) }
            }

            if (actions.isNotEmpty()) {
                toolWindow.setTitleActions(actions)
            }

            // Initial scan
            project.service<MainToolViewModel>().refresh()
        } catch (t: Throwable) {
            thisLogger().error("Failed to initialize Ktorfit & Retrofit Studio ToolWindow", t)
            val errorPanel = JPanel(BorderLayout()).apply {
                border = JBUI.Borders.empty(20)
                val label = JBLabel(
                    "<html><center><h3>Failed to load Ktorfit & Retrofit Studio</h3><p>${t.localizedMessage ?: t.javaClass.simpleName}</p></center></html>",
                    SwingConstants.CENTER
                )
                add(label, BorderLayout.CENTER)
                val retryButton = JButton("Retry").apply {
                    addActionListener {
                        toolWindow.contentManager.removeAllContents(true)
                        createToolWindowContent(project, toolWindow)
                    }
                }
                val reportIssueButton = JButton("Report on GitHub", AllIcons.Vcs.Vendors.Github).apply {
                    addActionListener {
                        PluginUiUtils.openBrowser("https://github.com/GeorgeNady/Retrofit_API_Swagger/issues/new")
                    }
                }
                val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
                    add(retryButton)
                    add(reportIssueButton)
                }
                add(buttonPanel, BorderLayout.SOUTH)
            }
            val content = ContentFactory.getInstance().createContent(errorPanel, "Error", false)
            toolWindow.contentManager.addContent(content)
        }
    }

    override fun shouldBeAvailable(project: Project) = true
}