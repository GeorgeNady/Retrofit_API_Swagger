package com.github.georgenady.retrofitApiSwagger.domain.edgeaction.engine

import com.github.georgenady.retrofitApiSwagger.model.ApiNode

/**
 * Pure Kotlin template evaluator for Edge Actions.
 * Replaces placeholders like ${source.methodName}, ${target.path},
 * ${source.methodName.toUpperCase()} or ${source.name.uppercase()}
 * without any external library dependencies or ClassLoader conflicts.
 */
class TemplateEvaluationEngine {

    private val tokenRegex = Regex("""(?:\$\{|\$)(source|target)\.([a-zA-Z0-9_]+)(?:\.([a-zA-Z0-9_]+)(?:\(\))?)?\}?""")

    fun evaluate(templateStr: String, source: ApiNode, target: ApiNode): String {
        return tokenRegex.replace(templateStr) { match ->
            val nodeRef = match.groupValues[1]
            val propName = match.groupValues[2]
            val transform = match.groupValues[3]

            val node = if (nodeRef == "source") source else target
            var value = when (propName) {
                "name", "methodName" -> node.methodName
                "httpMethod" -> node.httpMethod
                "path" -> node.path
                "className" -> node.className ?: ""
                "returnType", "returnTypeFqn" -> node.returnTypeFqn ?: "Unit"
                "signature" -> node.signature
                else -> match.value
            }

            if (transform.isNotBlank()) {
                if (transform.contains("upper", ignoreCase = true)) {
                    value = value.uppercase()
                } else if (transform.contains("lower", ignoreCase = true)) {
                    value = value.lowercase()
                }
            }

            value
        }.trim()
    }
}
