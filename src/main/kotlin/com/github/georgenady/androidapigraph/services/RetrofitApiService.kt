package com.github.georgenady.androidapigraph.services

import com.github.georgenady.androidapigraph.model.ApiNode
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
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
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)

        thisLogger().info("Starting Retrofit API scan for project: ${project.name}")

        // 1. Scan Kotlin Files using FileTypeIndex
        val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope)
        thisLogger().info("Found ${kotlinFiles.size} Kotlin files to scan.")
        kotlinFiles.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile)
            if (psiFile is KtFile) {
                scanKotlinFile(psiFile, endpoints)
            }
        }

        // 2. Scan Java Files using FileTypeIndex
        val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope)
        thisLogger().info("Found ${javaFiles.size} Java files to scan.")
        javaFiles.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile)
            if (psiFile is PsiJavaFile) {
                scanJavaFile(psiFile, endpoints)
            }
        }

        thisLogger().info("Scan complete. Found ${endpoints.size} endpoints.")
        return endpoints
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
        // Handle @GET("/path") or @GET(value = "/path")
        val valueArgument = annotation.valueArguments.firstOrNull { 
            it.getArgumentName() == null || it.getArgumentName()?.asName?.asString() == "value"
        } ?: return ""
        
        val expression = valueArgument.getArgumentExpression()
        return expression?.text?.removeSurrounding("\"") ?: ""
    }

    private fun extractJavaPath(annotation: PsiAnnotation): String {
        val attribute = annotation.findAttributeValue("value") ?: return ""
        return attribute.text.removeSurrounding("\"")
    }
}
