package com.github.georgenady.rettrofitapigraph.parser

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class JavaEndpointParser : FileEndpointParser {

    override fun canParse(psiFile: PsiFile): Boolean = psiFile is PsiJavaFile

    override fun parse(psiFile: PsiFile): List<ApiNode> {
        val javaFile = psiFile as? PsiJavaFile ?: return emptyList()
        return parseJavaFile(javaFile)
    }

    fun parseJavaFile(file: PsiJavaFile): List<ApiNode> {
        val endpoints = mutableListOf<ApiNode>()
        val classes = PsiTreeUtil.findChildrenOfType(file, PsiClass::class.java)

        for (psiClass in classes) {
            for (method in psiClass.methods) {
                val node = parseMethod(method, psiClass, file)
                if (node != null) {
                    endpoints.add(node)
                }
            }
        }
        return endpoints
    }

    fun parseMethod(method: PsiMethod, psiClass: PsiClass, file: PsiJavaFile): ApiNode? {
        var httpMethod: String? = null
        var path = ""
        var supportsCache = false

        for (annotation in method.annotations) {
            val qualifiedName = annotation.qualifiedName
            val shortName = qualifiedName?.substringAfterLast(".")
                ?: annotation.nameReferenceElement?.referenceName
                ?: continue

            if (shortName in RetrofitConstants.HTTP_METHODS) {
                httpMethod = shortName
                path = extractJavaPath(annotation, shortName)
            } else if (shortName == RetrofitConstants.SUPPORT_CACHE || shortName.endsWith(".${RetrofitConstants.SUPPORT_CACHE}")) {
                supportsCache = true
            }
        }

        if (httpMethod == null) return null

        return ApiNode(
            methodName = method.name,
            httpMethod = httpMethod,
            path = path,
            className = psiClass.name ?: file.name.substringBeforeLast("."),
            psiElement = method,
            supportsCache = supportsCache
        )
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
