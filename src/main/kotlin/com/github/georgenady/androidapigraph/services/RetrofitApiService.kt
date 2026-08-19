package com.github.georgenady.androidapigraph.services

import com.github.georgenady.androidapigraph.model.ApiNode
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

@Service(Service.Level.PROJECT)
class RetrofitApiService(private val project: Project) {

    private val retrofitAnnotations = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

    fun findRetrofitEndpoints(): List<ApiNode> {
        val endpoints = mutableListOf<ApiNode>()
        val scope = GlobalSearchScope.allScope(project)
        val psiManager = PsiManager.getInstance(project)

        thisLogger().info("Starting Retrofit API scan (Scope: All)")

        // 1. Try Index-based search first
        val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope)
        val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope)
        
        thisLogger().info("Index results: ${kotlinFiles.size} Kotlin files, ${javaFiles.size} Java files.")

        if (kotlinFiles.isEmpty() && javaFiles.isEmpty()) {
            thisLogger().warn("Indices are empty. Falling back to manual brute-force directory walk.")
            performManualBruteForceScan(endpoints)
        } else {
            kotlinFiles.forEach { vf -> scanFile(vf, psiManager, endpoints) }
            javaFiles.forEach { vf -> scanFile(vf, psiManager, endpoints) }
        }

        thisLogger().info("Scan complete. Found ${endpoints.size} total endpoints.")
        return endpoints
    }

    private fun scanFile(virtualFile: VirtualFile, psiManager: PsiManager, endpoints: MutableList<ApiNode>) {
        val psiFile = psiManager.findFile(virtualFile) ?: return
        when (psiFile) {
            is KtFile -> scanKotlinFile(psiFile, endpoints)
            is PsiJavaFile -> scanJavaFile(psiFile, endpoints)
        }
    }

    private fun performManualBruteForceScan(endpoints: MutableList<ApiNode>) {
        val psiManager = PsiManager.getInstance(project)
        val baseDir = project.baseDir ?: return
        
        VfsUtilCore.iterateChildrenRecursively(baseDir, null) { virtualFile ->
            if (!virtualFile.isDirectory) {
                val extension = virtualFile.extension
                if (extension == "kt" || extension == "java") {
                    scanFile(virtualFile, psiManager, endpoints)
                }
            }
            true
        }
    }

    private fun scanKotlinFile(file: KtFile, endpoints: MutableList<ApiNode>) {
        val functions = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
        functions.forEach { function ->
            function.annotationEntries.forEach { annotation ->
                val name = annotation.shortName?.asString()
                if (name in retrofitAnnotations) {
                    val path = extractKotlinPath(annotation)
                    val parentClass = PsiTreeUtil.getParentOfType(function, KtClass::class.java)
                    endpoints.add(ApiNode(
                        methodName = function.name ?: "unknown",
                        httpMethod = name!!,
                        path = path,
                        className = parentClass?.name ?: file.name,
                        psiElement = function
                    ))
                }
            }
        }
    }

    private fun scanJavaFile(file: PsiJavaFile, endpoints: MutableList<ApiNode>) {
        file.classes.forEach { psiClass ->
            psiClass.methods.forEach { method ->
                method.annotations.forEach { annotation ->
                    val qualifiedName = annotation.qualifiedName ?: ""
                    val shortName = qualifiedName.substringAfterLast(".")
                    if (shortName in retrofitAnnotations) {
                        val path = extractJavaPath(annotation)
                        endpoints.add(ApiNode(
                            methodName = method.name,
                            httpMethod = shortName,
                            path = path,
                            className = psiClass.name ?: file.name,
                            psiElement = method
                        ))
                    }
                }
            }
        }
    }

    private fun extractKotlinPath(annotation: KtAnnotationEntry): String {
        val valueArgument = annotation.valueArguments.firstOrNull { 
            it.getArgumentName() == null || it.getArgumentName()?.asName?.asString() == "value"
        } ?: return ""
        return valueArgument.getArgumentExpression()?.text?.removeSurrounding("\"") ?: ""
    }

    private fun extractJavaPath(annotation: PsiAnnotation): String {
        val attribute = annotation.findAttributeValue("value") ?: return ""
        return attribute.text.removeSurrounding("\"")
    }
}
