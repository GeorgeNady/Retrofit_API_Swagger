package com.github.georgenady.retrofitApiSwagger.domain.model

data class ApiFilterModel(
    val query: String = "",
    val methods: Set<String> = emptySet(),
    val module: String? = null,
    val customAnnotations: List<String> = emptyList()
) {
    fun matches(node: ApiNode): Boolean {
        if (query.isNotEmpty() && !node.path.contains(query, ignoreCase = true) && !node.methodName.contains(query, ignoreCase = true)) {
            return false
        }
        if (methods.isNotEmpty() && !methods.contains(node.httpMethod.uppercase())) {
            return false
        }
        if (module != null && node.className != module) { // Note: using className as a proxy for module for now
            return false
        }
        if (customAnnotations.isNotEmpty()) {
            val hasAllAnnotations = customAnnotations.all { target ->
                node.annotations.any { nodeAnnotation ->
                    nodeAnnotation.name.equals(target, ignoreCase = true)
                }
            }
            if (!hasAllAnnotations) {
                return false
            }
        }
        return true
    }
}
