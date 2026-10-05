package com.github.georgenady.retrofitApiSwagger.editor

import com.github.georgenady.retrofitApiSwagger.domain.repository.ApiRepository
import com.github.georgenady.retrofitApiSwagger.presentation.main.MainToolViewModel
import com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel.SwaggerPanel

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class DataSourceDesignEditor(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor, DumbAware {

    private val listPanel = SwaggerPanel(
        project = project,
        isEditorMode = true,
        targetFile = file
    )

    private var subscriptionJob: Job? = null

    init {
        setupReactiveRefresh()
    }

    private fun setupReactiveRefresh() {
        val apiService = project.service<ApiRepository>()
        val viewModel = project.service<MainToolViewModel>()

        // 1. Initial scan when smart
        DumbService.getInstance(project).runWhenSmart {
            refreshData(apiService)
        }

        val connection = project.messageBus.connect(this)

        // 2. Refresh whenever the IDE exits dumb mode (after indexing/syncing finishes)
        connection.subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
            override fun exitDumbMode() {
                ApplicationManager.getApplication().invokeLater {
                    refreshData(apiService)
                }
            }
        })

        // 3. Listen for VFS changes to this specific file
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                if (events.any { it.file == file }) {
                    ApplicationManager.getApplication().invokeLater {
                        refreshData(apiService)
                    }
                }
            }
        })

        // 4. Listen for in-memory PSI changes while typing in the editor
        PsiManager.getInstance(project).addPsiTreeChangeListener(object : PsiTreeChangeAdapter() {
            override fun childrenChanged(event: PsiTreeChangeEvent) {
                if (event.file?.virtualFile == file) {
                    ApplicationManager.getApplication().invokeLater {
                        refreshData(apiService)
                    }
                }
            }
        }, this)

        // 5. Observe project-level scan results and update if this file has endpoints
        subscriptionJob = viewModel.viewModelScope.launch(Dispatchers.Main) {
            viewModel.uiState.collect { state ->
                if (!file.isValid) return@collect
                val matching = state.allEndpoints.filter {
                    it.psiElement?.containingFile?.virtualFile == file ||
                    it.className == file.nameWithoutExtension
                }
                if (matching.isNotEmpty()) {
                    listPanel.render(matching)
                }
            }
        }
    }

    private fun refreshData(apiService: ApiRepository) {
        if (!file.isValid) return
        val endpoints = apiService.findRetrofitEndpointsInFile(file)
        listPanel.render(endpoints)

        // If endpoints list is empty and IDE might still be indexing or initializing, schedule a smart retry
        if (endpoints.isEmpty()) {
            DumbService.getInstance(project).smartInvokeLater {
                if (file.isValid) {
                    val retry = apiService.findRetrofitEndpointsInFile(file)
                    if (retry.isNotEmpty()) {
                        listPanel.render(retry)
                    }
                }
            }
        }
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
    
    override fun dispose() {
        subscriptionJob?.cancel()
        subscriptionJob = null
    }
}