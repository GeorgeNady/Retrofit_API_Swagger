package com.github.georgenady.rettrofitapigraph.data.parser

import com.github.georgenady.rettrofitapigraph.data.parser.utils.RetrofitConstants
import com.github.georgenady.rettrofitapigraph.domain.model.*
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*

class KotlinEndpointParser : FileEndpointParser {

    override fun canParse(psiFile: PsiFile): Boolean = psiFile is KtFile

    override fun parse(psiFile: PsiFile): List<ApiNode> {
        val ktFile = psiFile as? KtFile ?: return emptyList()
        return parseKtFile(ktFile)
    }

    fun parseKtFile(file: KtFile): List<ApiNode> {
        val endpoints = mutableListOf<ApiNode>()
        val functions = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)

        for (function in functions) {
            val node = parseFunction(function, file)
            if (node != null) {
                endpoints.add(node)
            }
        }
        return endpoints
    }

    fun parseFunction(function: KtNamedFunction, file: KtFile): ApiNode? {
        var httpMethod: String? = null
        var path = ""
        var supportsCache = false
        val allAnnotations = mutableListOf<AnnotationDetail>()

        for (annotation in function.annotationEntries) {
            val name = annotation.shortName?.asString() ?: continue
            val args = extractKotlinAnnotationArgs(annotation)
            allAnnotations.add(AnnotationDetail(name, args))

            if (name in RetrofitConstants.HTTP_METHODS) {
                httpMethod = name
                path = extractKotlinPath(annotation, name)
            } else if (name.startsWith(RetrofitConstants.RETROFIT_PACKAGE_PREFIX)) {
                val simpleName = name.removePrefix(RetrofitConstants.RETROFIT_PACKAGE_PREFIX)
                if (simpleName in RetrofitConstants.HTTP_METHODS) {
                    httpMethod = simpleName
                    path = extractKotlinPath(annotation, simpleName)
                }
            } else if (name == RetrofitConstants.SUPPORT_CACHE || name.endsWith(".${RetrofitConstants.SUPPORT_CACHE}")) {
                supportsCache = true
            }
        }

        if (httpMethod == null) return null

        val parentClass = PsiTreeUtil.getParentOfType(function, KtClassOrObject::class.java)
        val className = parentClass?.name ?: file.name.substringBeforeLast(".")

        val parameters = function.valueParameters.map { param ->
            var location = ParameterLocation.QUERY
            
            for (anno in param.annotationEntries) {
                val shortName = anno.shortName?.asString()
                location = when (shortName) {
                    "Path" -> ParameterLocation.PATH
                    "Query", "QueryMap" -> ParameterLocation.QUERY
                    "Header", "HeaderMap" -> ParameterLocation.HEADER
                    "Body" -> ParameterLocation.BODY
                    else -> location
                }
            }

            ParameterDetail(
                name = param.name ?: "unnamed",
                type = param.typeReference?.text ?: "Any",
                location = location,
                fqn = null // Simplified for now to fix build
            )
        }

        return ApiNode(
            methodName = function.name ?: "unknownMethod",
            httpMethod = httpMethod,
            path = path,
            className = className,
            psiElement = function,
            supportsCache = supportsCache,
            annotations = allAnnotations,
            parameters = parameters,
            returnTypeFqn = null // Simplified for now to fix build
        )
    }

    private fun extractKotlinAnnotationArgs(annotation: KtAnnotationEntry): Map<String, String> {
        val args = mutableMapOf<String, String>()
        annotation.valueArguments.forEachIndexed { index, arg ->
            val name = arg.getArgumentName()?.asName?.asString() ?: "value_$index"
            val value = arg.getArgumentExpression()?.text?.removeSurrounding("\"") ?: ""
            args[name] = value
        }
        return args
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

    private fun cleanQuotes(text: String): String {
        return text.trim()
            .removeSurrounding("\"\"\"")
            .removeSurrounding("\"")
    }
}
