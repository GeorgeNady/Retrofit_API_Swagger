package com.github.georgenady.rettrofitapigraph.services

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.model.ScanResult
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

@Service(Service.Level.PROJECT)
class RetrofitApiService(private val project: Project) {

    companion object {
        val HTTP_METHODS = setOf(
            "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "HTTP"
        )
        val RETROFIT_PACKAGE_PREFIX = "retrofit2.http."
    }

    fun findRetrofitEndpoints(): ScanResult {
        val startTime = System.currentTimeMillis()
        val isDumb = DumbService.isDumb(project)

        if (isDumb) {
            thisLogger().warn("Project is currently in dumb mode (indexing). Scan results might be incomplete.")
            return ScanResult(emptyList(), 0, 0, true)
        }

        return ReadAction.compute<ScanResult, Throwable> {
            val endpoints = mutableListOf<ApiNode>()
            val psiManager = PsiManager.getInstance(project)

            // Use allScope for maximum coverage
            val scope = GlobalSearchScope.allScope(project)

            val fileIndex = ProjectRootManager.getInstance(project).fileIndex
            val processedFiles = mutableSetOf<VirtualFile>()

            // 1. Collect Kotlin files
            val ktFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope)
            thisLogger().info("Index found ${ktFiles.size} Kotlin files.")
            for (virtualFile in ktFiles) {
                if (!processedFiles.add(virtualFile)) continue
                val psiFile = psiManager.findFile(virtualFile) as? KtFile ?: continue
                scanKotlinFile(psiFile, endpoints)
            }

            // 2. Collect Java files
            val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope)
            thisLogger().info("Index found ${javaFiles.size} Java files.")
            for (virtualFile in javaFiles) {
                if (!processedFiles.add(virtualFile)) continue
                val psiFile = psiManager.findFile(virtualFile) as? PsiJavaFile ?: continue
                scanJavaFile(psiFile, endpoints)
            }

            // 3. Brute-force fallback for multi-module projects where indices might miss some content
            if (processedFiles.size < 5) {
                thisLogger().info("Index results too low (${processedFiles.size}). Performing manual module walk.")
                com.intellij.openapi.module.ModuleManager.getInstance(project).modules.forEach { module ->
                    com.intellij.openapi.roots.ModuleRootManager.getInstance(module).contentRoots.forEach { root ->
                        com.intellij.openapi.vfs.VfsUtilCore.iterateChildrenRecursively(root, null) { vf ->
                            if (!vf.isDirectory && (vf.extension == "kt" || vf.extension == "java")) {
                                if (processedFiles.add(vf)) {
                                    val psiFile = psiManager.findFile(vf)
                                    if (psiFile is KtFile) scanKotlinFile(psiFile, endpoints)
                                    else if (psiFile is PsiJavaFile) scanJavaFile(psiFile, endpoints)
                                }
                            }
                            true
                        }
                    }
                }
            }
            val totalFilesCount = processedFiles.size
            val duration = System.currentTimeMillis() - startTime
            thisLogger().info("Scan finished: Scanned $totalFilesCount files, found ${endpoints.size} endpoints in ${duration}ms.")
            ScanResult(endpoints, totalFilesCount, duration, false)
        }
    }

    fun scanKotlinFile(file: KtFile, endpoints: MutableList<ApiNode>) {
        val functions = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)

        for (function in functions) {
            var httpMethod: String? = null
            var path = ""
            var supportsCache = false

            for (annotation in function.annotationEntries) {
                val name = annotation.shortName?.asString() ?: continue

                if (name in HTTP_METHODS) {
                    httpMethod = name
                    path = extractKotlinPath(annotation, name)
                } else if (name.startsWith(RETROFIT_PACKAGE_PREFIX)) {
                    val simpleName = name.removePrefix(RETROFIT_PACKAGE_PREFIX)
                    if (simpleName in HTTP_METHODS) {
                        httpMethod = simpleName
                        path = extractKotlinPath(annotation, simpleName)
                    }
                } else if (name == "SupportCache" || name.endsWith(".SupportCache")) {
                    supportsCache = true
                }
            }

            if (httpMethod != null) {
                val parentClass = PsiTreeUtil.getParentOfType(function, KtClassOrObject::class.java)
                val className = parentClass?.name ?: file.name.substringBeforeLast(".")

                endpoints.add(
                    ApiNode(
                        methodName = function.name ?: "unknownMethod",
                        httpMethod = httpMethod,
                        path = path,
                        className = className,
                        psiElement = function,
                        supportsCache = supportsCache
                    )
                )
            }
        }
    }

    fun scanJavaFile(file: PsiJavaFile, endpoints: MutableList<ApiNode>) {
        val classes = PsiTreeUtil.findChildrenOfType(file, PsiClass::class.java)

        for (psiClass in classes) {
            for (method in psiClass.methods) {
                var httpMethod: String? = null
                var path = ""
                var supportsCache = false

                for (annotation in method.annotations) {
                    val qualifiedName = annotation.qualifiedName
                    val shortName = qualifiedName?.substringAfterLast(".")
                        ?: annotation.nameReferenceElement?.referenceName
                        ?: continue

                    if (shortName in HTTP_METHODS) {
                        httpMethod = shortName
                        path = extractJavaPath(annotation, shortName)
                    } else if (shortName == "SupportCache") {
                        supportsCache = true
                    }
                }

                if (httpMethod != null) {
                    endpoints.add(
                        ApiNode(
                            methodName = method.name,
                            httpMethod = httpMethod,
                            path = path,
                            className = psiClass.name ?: file.name.substringBeforeLast("."),
                            psiElement = method,
                            supportsCache = supportsCache
                        )
                    )
                }
            }
        }
    }

    private fun extractKotlinPath(annotation: KtAnnotationEntry, httpMethod: String): String {
        if (httpMethod == "HTTP") {
            val pathArg = annotation.valueArguments.find { it.getArgumentName()?.asName?.asString() == "path" }
                ?: annotation.valueArguments.getOrNull(1)
            val text = pathArg?.getArgumentExpression()?.text ?: ""
            return cleanQuotes(text)
        }

        val argument = annotation.valueArguments.firstOrNull {
            it.getArgumentName() == null || it.getArgumentName()?.asName?.asString() == "value"
        } ?: return ""

        val expression = argument.getArgumentExpression()
        val text = expression?.text ?: return ""
        return cleanQuotes(text)
    }

    private fun extractJavaPath(annotation: PsiAnnotation, httpMethod: String): String {
        if (httpMethod == "HTTP") {
            val pathAttr = annotation.findAttributeValue("path")
            if (pathAttr != null) {
                return cleanQuotes(pathAttr.text)
            }
        }

        val valueAttribute = annotation.findAttributeValue("value")
        return if (valueAttribute != null) cleanQuotes(valueAttribute.text) else ""
    }

    private fun cleanQuotes(text: String): String {
        return text.trim()
            .removeSurrounding("\"\"\"")
            .removeSurrounding("\"")
    }
}