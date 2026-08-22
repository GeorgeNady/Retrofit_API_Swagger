package com.github.georgenady.rettrofitapigraph.editor

import com.github.georgenady.rettrofitapigraph.domain.repository.ApiRepository
import com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.SwaggerPanel
import com.github.georgenady.rettrofitapigraph.presentation.viewmodel.ApiDashboardViewModel
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class DataSourceDesignEditor(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor {

    private val viewModel = project.service<ApiDashboardViewModel>()

    private val listPanel = SwaggerPanel(project) { selectedNode ->
        viewModel.selectNode(selectedNode)
    }

    init {
        // Fetch only the APIs for THIS specific file
        val apiService = project.service<ApiRepository>()
        
        // You will need to add a method to your service to scan a single file:
        val fileEndpoints = apiService.findRetrofitEndpointsInFile(file)
        
        listPanel.render(fileEndpoints)
    }

    override fun getComponent(): JComponent = listPanel
    
    override fun getPreferredFocusedComponent(): JComponent = listPanel
    
    override fun getName(): String = "Design"
    
    override fun setState(state: FileEditorState) {}
    
    override fun isModified(): Boolean = false
    
    override fun isValid(): Boolean = true
    
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    
    override fun getCurrentLocation(): FileEditorLocation? = null
    
    override fun dispose() {}
}