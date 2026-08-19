package com.github.georgenady.rettrofitapigraph.parser

import com.github.georgenady.rettrofitapigraph.model.AnnotationDetail
import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.model.ParameterDetail
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
        val allAnnotations = mutableListOf<AnnotationDetail>()

        for (annotation in method.annotations) {
            val qualifiedName = annotation.qualifiedName
            val shortName = qualifiedName?.substringAfterLast(".")
                ?: annotation.nameReferenceElement?.referenceName
                ?: continue
            
            val args = extractJavaAnnotationArgs(annotation)
            allAnnotations.add(AnnotationDetail(shortName, args))

            if (shortName in RetrofitConstants.HTTP_METHODS) {
                httpMethod = shortName
                path = extractJavaPath(annotation, shortName)
            } else if (shortName == RetrofitConstants.SUPPORT_CACHE || shortName.endsWith(".${RetrofitConstants.SUPPORT_CACHE}")) {
                supportsCache = true
            }
        }

        if (httpMethod == null) return null

        val parameters = if (httpMethod != "GET") {
            method.parameterList.parameters.map { 
                ParameterDetail(it.name, it.type.presentableText)
            }
        } else emptyList()

        return ApiNode(
            methodName = method.name,
            httpMethod = httpMethod!!,
            path = path,
            className = psiClass.name ?: file.name.substringBeforeLast("."),
            psiElement = method,
            supportsCache = supportsCache,
            annotations = allAnnotations,
            parameters = parameters
        )
    }

    private fun extractJavaAnnotationArgs(annotation: PsiAnnotation): Map<String, String> {
        val args = mutableMapOf<String, String>()
        annotation.parameterList.attributes.forEach { attr ->
            val name = attr.name ?: "value"
            val value = attr.value?.text?.removeSurrounding("\"") ?: ""
            args[name] = value
        }
        return args
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
