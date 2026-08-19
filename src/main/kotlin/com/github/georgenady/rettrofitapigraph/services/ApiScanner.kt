package com.github.georgenady.rettrofitapigraph.services

import com.github.georgenady.rettrofitapigraph.model.ApiEndpoint
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class ApiScanner(private val project: Project) {

    fun scanForRetrofitApis(): List<ApiEndpoint> {
        val endpoints = mutableListOf<ApiEndpoint>()

        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)
        val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope)

        val visitor = object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)

                function.annotationEntries.forEach { annotation ->
                    val shortName = annotation.shortName?.asString() ?: ""
                    if (shortName in listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD")) {

                        val parentClass = function.getParentOfType<KtClassOrObject>(strict = true)
                        val className = parentClass?.name ?: "Global APIs"
                        val path = extractPathFromAnnotation(annotation.text)

                        endpoints.add(
                            ApiEndpoint(
                                httpMethod = shortName,
                                path = path,
                                className = className,
                                methodName = function.name ?: "unnamedFunction", // Fixed: Pass methodName
                                psiElement = function
                            )
                        )
                    }
                }
            }
        }

        for (virtualFile in kotlinFiles) {
            val psiFile = psiManager.findFile(virtualFile)
            if (psiFile is KtFile) {
                psiFile.accept(visitor)
            }
        }

        return endpoints
    }

    private fun extractPathFromAnnotation(annotationText: String): String {
        return annotationText.substringAfter("\"").substringBeforeLast("\"")
    }
}