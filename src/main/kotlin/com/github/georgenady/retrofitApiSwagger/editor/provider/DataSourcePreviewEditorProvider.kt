package com.github.georgenady.retrofitApiSwagger.editor.provider

import com.github.georgenady.retrofitApiSwagger.editor.DataSourceDesignEditor
import com.github.georgenady.retrofitApiSwagger.editor.model.DataSourceSplitEditor
import com.intellij.openapi.fileEditor.AsyncFileEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class DataSourcePreviewEditorProvider : AsyncFileEditorProvider, DumbAware {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        val extension = file.extension
        if (extension != "kt" && extension != "java") return false

        // Fast check to avoid parsing the AST on the UI thread for every file clicked.
        // We just read the raw text and check if it imports retrofit or uses annotations.
        val fileContent = String(file.contentsToByteArray())
        return fileContent.contains("@GET", false) ||
               fileContent.contains("@POST", false) ||
               fileContent.contains("retrofit2.http", false)
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return createEditorAsync(project, file).build()
    }

    override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder {
        return object : AsyncFileEditorProvider.Builder() {
            override fun build(): FileEditor {
                // 1. Get the standard text editor (Code View)
                val textEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor

                // 2. Create our custom graph editor (Design View)
                val designEditor = DataSourceDesignEditor(project, file)

                // 3. Combine them into the Split View
                return DataSourceSplitEditor(textEditor, designEditor)
            }
        }
    }

    override fun getEditorTypeId(): String = "retrofit-api-preview-editor"

    // PLACE_AFTER_DEFAULT_EDITOR ensures our split view wraps the default text view
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}