package com.github.georgenady.rettrofitapigraph.data.parser

import com.github.georgenady.rettrofitapigraph.data.parser.utils.RetrofitConstants
import com.github.georgenady.rettrofitapigraph.domain.model.*
import com.intellij.psi.*
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

        val parameters = method.parameterList.parameters.map { param ->
            var location = ParameterLocation.QUERY
            
            for (anno in param.annotations) {
                val shortName = anno.qualifiedName?.substringAfterLast(".")
                location = when (shortName) {
                    "Path" -> ParameterLocation.PATH
                    "Query", "QueryMap" -> ParameterLocation.QUERY
                    "Header", "HeaderMap" -> ParameterLocation.HEADER
                    "Body" -> ParameterLocation.BODY
                    else -> location
                }
            }

            ParameterDetail(
                name = param.name,
                type = param.type.presentableText,
                location = location,
                fqn = (param.type as? PsiClassType)?.resolve()?.qualifiedName
            )
        }

        return ApiNode(
            methodName = method.name,
            httpMethod = httpMethod,
            path = path,
            className = psiClass.name ?: file.name.substringBeforeLast("."),
            psiElement = method,
            supportsCache = supportsCache,
            annotations = allAnnotations,
            parameters = parameters,
            returnTypeFqn = (method.returnType as? PsiClassType)?.resolve()?.qualifiedName
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
