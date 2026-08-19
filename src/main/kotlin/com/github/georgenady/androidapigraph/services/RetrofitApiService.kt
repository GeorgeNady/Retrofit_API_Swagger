package com.github.georgenady.androidapigraph.services

import com.github.georgenady.androidapigraph.model.ApiNode
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

@Service(Service.Level.PROJECT)
class RetrofitApiService(private val project: Project) {

    private val retrofitAnnotations = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

    fun findRetrofitEndpoints(): List<ApiNode> {
        val endpoints = mutableListOf<ApiNode>()
        
        // Use ProjectRootManager to find source roots and iterate
        val projectRootManager = ProjectRootManager.getInstance(project)
        val psiManager = PsiManager.getInstance(project)
        
        projectRootManager.contentRoots.forEach { root ->
            val directory = psiManager.findDirectory(root)
            if (directory != null) {
                traverseDirectory(directory, endpoints)
            }
        }

        return endpoints
    }

    private fun traverseDirectory(directory: PsiDirectory, endpoints: MutableList<ApiNode>) {
        directory.files.forEach { file ->
            when (file) {
                is KtFile -> scanKotlinFile(file, endpoints)
                is PsiJavaFile -> scanJavaFile(file, endpoints)
            }
        }
        directory.subdirectories.forEach { traverseDirectory(it, endpoints) }
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
                        className = parentClass?.name ?: "Unknown",
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
                            className = psiClass.name ?: "Unknown",
                            psiElement = method
                        ))
                    }
                }
            }
        }
    }

    private fun extractKotlinPath(annotation: KtAnnotationEntry): String {
        val valueArgument = annotation.valueArguments.firstOrNull() ?: return ""
        val expression = valueArgument.getArgumentExpression()
        return expression?.text?.removeSurrounding("\"") ?: ""
    }

    private fun extractJavaPath(annotation: PsiAnnotation): String {
        val attribute = annotation.findAttributeValue("value") ?: return ""
        return attribute.text.removeSurrounding("\"")
    }
}
