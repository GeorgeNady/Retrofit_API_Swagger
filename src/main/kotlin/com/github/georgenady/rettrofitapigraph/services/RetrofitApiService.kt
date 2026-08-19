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

        thisLogger().info("Starting Aggressive Retrofit API scan")

        // 1. Index-based search (Fast)
        val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope)
        val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope)
        
        thisLogger().info("Index results: ${kotlinFiles.size} Kotlin, ${javaFiles.size} Java files.")
        
        kotlinFiles.forEach { vf -> scannedFiles.add(vf) }
        javaFiles.forEach { vf -> scannedFiles.add(vf) }

        // 2. Module Content Root walk (Aggressive for multi-module)
        val moduleManager = ModuleManager.getInstance(project)
        moduleManager.modules.forEach { module ->
            val rootManager = ModuleRootManager.getInstance(module)
            rootManager.contentRoots.forEach { root ->
                VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
                    if (!virtualFile.isDirectory) {
                        val ext = virtualFile.extension
                        if (ext == "kt" || ext == "java") {
                            scannedFiles.add(virtualFile)
                        }
                    }
                    true
                }
            }
        }

        thisLogger().info("Total unique files identified for scanning: ${scannedFiles.size}")

        scannedFiles.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile) ?: return@forEach
            when (psiFile) {
                is KtFile -> scanKotlinFile(psiFile, endpoints)
                is PsiJavaFile -> scanJavaFile(psiFile, endpoints)
            }
        }

        val duration = System.currentTimeMillis() - startTime
        thisLogger().info("Scan complete. Found ${endpoints.size} endpoints in ${scannedFiles.size} files in ${duration}ms.")
        
        return ScanResult(endpoints, scannedFiles.size, duration)
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
