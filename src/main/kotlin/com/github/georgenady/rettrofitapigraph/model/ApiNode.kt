package com.github.georgenady.rettrofitapigraph.model

import com.intellij.psi.PsiElement

data class AnnotationDetail(
    val name: String,
    val arguments: Map<String, String>
)

data class ParameterDetail(
    val name: String,
    val type: String
)

data class ApiNode(
    val methodName: String,
    val httpMethod: String,
    val path: String,
    val className: String,
    val psiElement: PsiElement? = null,
    val supportsCache: Boolean = false,
    val invalidatesKeys: List<String> = emptyList(),
    val annotations: List<AnnotationDetail> = emptyList(),
    val parameters: List<ParameterDetail> = emptyList()
)
