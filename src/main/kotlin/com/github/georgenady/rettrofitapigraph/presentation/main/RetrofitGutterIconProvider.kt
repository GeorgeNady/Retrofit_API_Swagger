package com.github.georgenady.rettrofitapigraph.presentation.main

import com.github.georgenady.rettrofitapigraph.domain.repository.ApiRepository
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNameIdentifierOwner
import org.jetbrains.kotlin.psi.KtNamedFunction

class RetrofitGutterIconProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val isEndpoint = when (element) {
            is KtNamedFunction -> element.annotationEntries.any { it.shortName?.asString()?.matches(Regex("GET|POST|PUT|DELETE|PATCH|HTTP")) == true }
            is PsiMethod -> element.annotations.any { it.qualifiedName?.contains("retrofit2.http") == true }
            else -> false
        }

        if (!isEndpoint) return null

        val nameIdentifier = (element as? PsiNameIdentifierOwner)?.nameIdentifier ?: return null

        return LineMarkerInfo(
            nameIdentifier,
            nameIdentifier.textRange,
            AllIcons.Actions.Execute,
            { "Open and test in Api Swagger" },
            { _, elt ->
                val project = elt.project
                val virtualFile = elt.containingFile.virtualFile ?: return@LineMarkerInfo
                val apiRepository = project.service<ApiRepository>()
                val viewModel = project.service<MainToolViewModel>()
                
                val nodes = apiRepository.findRetrofitEndpointsInFile(virtualFile)
                val targetNode = nodes.find { it.psiElement == element || it.methodName == element.name }
                
                if (targetNode != null) {
                    viewModel.selectNode(targetNode)
                    viewModel.expandNode(targetNode)
                    
                    val fileEditorManager = FileEditorManager.getInstance(project)
                    fileEditorManager.openFile(virtualFile, true)
                    
                    val editors = fileEditorManager.getEditors(virtualFile)
                    editors.filterIsInstance<TextEditorWithPreview>().forEach { splitEditor ->
                        splitEditor.setLayout(TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW)
                    }
                }
            },
            GutterIconRenderer.Alignment.LEFT,
            { "Retrofit Endpoint" }
        )
    }
}
