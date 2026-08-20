package com.github.georgenady.rettrofitapigraph.editor

import com.github.georgenady.rettrofitapigraph.services.RetrofitApiService
import com.github.georgenady.rettrofitapigraph.ui.graph.ApiGraphPanel
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class RetrofitDesignEditor(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor {

    // Reuse your existing Graph Panel here
    private val graphPanel = ApiGraphPanel(project) { selectedNode ->
        // Handle navigation if needed
    }

    init {
        // Fetch only the APIs for THIS specific file
        val apiService = project.service<RetrofitApiService>()
        
        // You will need to add a method to your service to scan a single file:
        val fileEndpoints = apiService.findRetrofitEndpointsInFile(file)
        
        graphPanel.render(fileEndpoints)
    }

    override fun getComponent(): JComponent = graphPanel
    
    override fun getPreferredFocusedComponent(): JComponent = graphPanel
    
    override fun getName(): String = "Design"
    
    override fun setState(state: FileEditorState) {}
    
    override fun isModified(): Boolean = false
    
    override fun isValid(): Boolean = true
    
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    
    override fun getCurrentLocation(): FileEditorLocation? = null
    
    override fun dispose() {}
}