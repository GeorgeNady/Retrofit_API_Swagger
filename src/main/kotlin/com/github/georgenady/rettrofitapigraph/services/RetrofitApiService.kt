package com.github.georgenady.rettrofitapigraph.services

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.module.ModuleManager
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

data class ScanResult(
    val endpoints: List<ApiNode>,
    val filesScanned: Int,
    val durationMs: Long
)

@Service(Service.Level.PROJECT)
class RetrofitApiService(private val project: Project) {

    private val retrofitAnnotations = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

    fun findRetrofitEndpoints(): ScanResult {
        val startTime = System.currentTimeMillis()
        val endpoints = mutableListOf<ApiNode>()
        val scannedFiles = mutableSetOf<VirtualFile>()
        
        val scope = GlobalSearchScope.allScope(project)
        val psiManager = PsiManager.getInstance(project)

        val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope)
        val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope)
        
        kotlinFiles.forEach { vf -> scannedFiles.add(vf) }
        javaFiles.forEach { vf -> scannedFiles.add(vf) }

        scannedFiles.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile) ?: return@forEach
            when (psiFile) {
                is KtFile -> scanKotlinFile(psiFile, endpoints)
                is PsiJavaFile -> scanJavaFile(psiFile, endpoints)
            }
        }

        val duration = System.currentTimeMillis() - startTime
        return ScanResult(endpoints, scannedFiles.size, duration)
    }

    private fun scanKotlinFile(file: KtFile, endpoints: MutableList<ApiNode>) {
        val functions = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
        functions.forEach { function ->
            var httpMethod: String? = null
            var path = ""
            var supportsCache = false
            val invalidatesKeys = mutableListOf<String>()

            function.annotationEntries.forEach { annotation ->
                val name = annotation.shortName?.asString()
                if (name in retrofitAnnotations) {
                    httpMethod = name
                    path = extractKotlinPath(annotation)
                } else if (name == "SupportCache") {
                    supportsCache = true
                } else if (name == "InvalidateCache") {
                    // Extract keys from annotation
                }
            }

            if (httpMethod != null) {
                val parentClass = PsiTreeUtil.getParentOfType(function, KtClass::class.java)
                endpoints.add(ApiNode(
                    methodName = function.name ?: "unknown",
                    httpMethod = httpMethod!!,
                    path = path,
                    className = parentClass?.name ?: file.name,
                    psiElement = function,
                    supportsCache = supportsCache,
                    invalidatesKeys = invalidatesKeys
                ))
            }
        }
    }

    private fun scanJavaFile(file: PsiJavaFile, endpoints: MutableList<ApiNode>) {
        file.classes.forEach { psiClass ->
            psiClass.methods.forEach { method ->
                var httpMethod: String? = null
                var path = ""
                
                method.annotations.forEach { annotation ->
                    val shortName = annotation.qualifiedName?.substringAfterLast(".") ?: ""
                    if (shortName in retrofitAnnotations) {
                        httpMethod = shortName
                        path = extractJavaPath(annotation)
                    }
                }

                if (httpMethod != null) {
                    endpoints.add(ApiNode(
                        methodName = method.name,
                        httpMethod = httpMethod!!,
                        path = path,
                        className = psiClass.name ?: file.name,
                        psiElement = method
                    ))
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
