package com.github.georgenady.rettrofitapigraph.services

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.model.ScanResult
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
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

    fun findRetrofitEndpoints(): ScanResult {
        // We use a shorter cache during development or disable it to ensure freshness
        val result = performScan()
        thisLogger().info("Scan complete: found ${result.endpoints.size} endpoints in ${result.filesScanned} files.")
        return result
    }

    private fun performScan(): ScanResult {
        val startTime = System.currentTimeMillis()
        val endpoints = mutableListOf<ApiNode>()
        val scannedFiles = mutableSetOf<VirtualFile>()
        val isDumb = DumbService.isDumb(project)
        
        val scope = GlobalSearchScope.allScope(project)
        val psiManager = PsiManager.getInstance(project)

        thisLogger().info("Scanning project: ${project.name} (Dumb Mode: $isDumb)")

        // 1. Index-based search (Only works in Smart Mode)
        if (!isDumb) {
            try {
                FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope).forEach { scannedFiles.add(it) }
                FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope).forEach { scannedFiles.add(it) }
                thisLogger().info("Index found ${scannedFiles.size} files.")
            } catch (e: Exception) {
                thisLogger().warn("Index access failed: ${e.message}")
            }
        }

        // 2. Brute-force fallback (Always do this if index is small or empty)
        if (scannedFiles.size < 10) {
            thisLogger().info("Performing manual module root walk for more results.")
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
        }

        thisLogger().info("Total files to analyze: ${scannedFiles.size}")

        scannedFiles.forEach { virtualFile ->
            try {
                val psiFile = psiManager.findFile(virtualFile)
                if (psiFile is KtFile) {
                    scanKotlinFile(psiFile, endpoints)
                } else if (psiFile is PsiJavaFile) {
                    scanJavaFile(psiFile, endpoints)
                }
            } catch (e: Exception) {
                thisLogger().error("Failed to scan file: ${virtualFile.path}", e)
            }
        }

        val duration = System.currentTimeMillis() - startTime
        return ScanResult(endpoints, scannedFiles.size, duration, isDumb)
    }

    private fun scanKotlinFile(file: KtFile, endpoints: MutableList<ApiNode>) {
        val functions = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
        functions.forEach { function ->
            var httpMethod: String? = null
            var path = ""
            var supportsCache = false

            function.annotationEntries.forEach { annotation ->
                val name = annotation.shortName?.asString()
                if (name in retrofitAnnotations) {
                    httpMethod = name
                    path = extractKotlinPath(annotation)
                } else if (name == "SupportCache") {
                    supportsCache = true
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
                    supportsCache = supportsCache
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
