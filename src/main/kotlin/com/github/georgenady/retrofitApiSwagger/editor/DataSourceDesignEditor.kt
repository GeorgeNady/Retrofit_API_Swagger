package com.github.georgenady.retrofitApiSwagger.editor

import com.github.georgenady.retrofitApiSwagger.domain.repository.ApiRepository
import com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel.SwaggerPanel
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class DataSourceDesignEditor(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor, DumbAware {

    private val listPanel = SwaggerPanel(
        project = project
    )

    init {
        setupReactiveRefresh()
    }

    private fun setupReactiveRefresh() {
        val apiService = project.service<ApiRepository>()
        
        // Initial scan when smart
        com.intellij.openapi.project.DumbService.getInstance(project).runWhenSmart {
            refreshData(apiService)
        }

        // Listen for VFS changes to this specific file
        val connection = project.messageBus.connect(this)
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                if (events.any { it.file == file }) {
                    ApplicationManager.getApplication().invokeLater {
                        refreshData(apiService)
                    }
                }
            }
        })
    }

    private fun refreshData(apiService: ApiRepository) {
        if (!file.isValid) return
        val endpoints = apiService.findRetrofitEndpointsInFile(file)
        listPanel.render(endpoints)
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