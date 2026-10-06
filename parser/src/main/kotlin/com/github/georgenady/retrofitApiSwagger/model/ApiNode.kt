package com.github.georgenady.retrofitApiSwagger.model

import com.intellij.psi.PsiElement

/**
 * Domain entity representing a discovered Retrofit API endpoint.
 *
 * @property methodName The name of the Java/Kotlin method defining the endpoint.
 * @property httpMethod The HTTP verb (GET, POST, PUT, DELETE, etc.).
 * @property path The relative URL path of the endpoint.
 * @property className The enclosing class or interface name.
 * @property psiElement The underlying PSI element corresponding to this endpoint definition.
 * @property supportsCache Whether caching is supported on this endpoint.
 * @property invalidatesKeys Cache keys invalidated when this endpoint is called.
 * @property annotations List of annotations associated with this endpoint method.
 * @property parameters List of parameters defined on this endpoint method.
 * @property returnTypeFqn The fully-qualified return type name, if resolvable.
 */
data class ApiNode(
    val methodName: String,
    val httpMethod: String,
    val path: String,
    val className: String,
    val psiElement: PsiElement? = null,
    val supportsCache: Boolean = false,
    val invalidatesKeys: List<String> = emptyList(),
    val annotations: List<AnnotationDetail> = emptyList(),
    val parameters: List<ParameterDetail> = emptyList(),
    val returnTypeFqn: String? = null
) {
    /**
     * Unique human-readable signature for identifying the endpoint across the project.
     */
    val signature: String get() = "$className.$methodName[$httpMethod]($path)"
}
