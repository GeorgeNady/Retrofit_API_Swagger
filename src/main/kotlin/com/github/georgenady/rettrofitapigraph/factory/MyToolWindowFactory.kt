package com.github.georgenady.rettrofitapigraph.factory

import com.github.georgenady.rettrofitapigraph.actions.RefreshApiGraphAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.github.georgenady.rettrofitapigraph.services.RetrofitApiService
import com.github.georgenady.rettrofitapigraph.toolWindow.MyToolWindow
import com.github.georgenady.rettrofitapigraph.ui.ApiGraphComponent
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class MyToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getComponent(), null, false)
        toolWindow.contentManager.addContent(content)
        
        // Add Refresh action to tool window header
        val actionManager = ActionManager.getInstance()
        val refreshAction = actionManager.getAction(RefreshApiGraphAction::class.java.canonicalName)
        if (refreshAction != null) {
            toolWindow.setTitleActions(listOf(refreshAction))
        }
    }

    override fun shouldBeAvailable(project: Project) = true


}
