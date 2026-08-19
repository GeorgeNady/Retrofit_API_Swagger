package com.github.georgenady.rettrofitapigraph.model

data class ApiFilterModel(
    val query: String = "",
    val methods: Set<String> = emptySet(),
    val module: String? = null
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
        return true
    }
}
