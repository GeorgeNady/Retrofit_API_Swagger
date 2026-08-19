package com.github.georgenady.rettrofitapigraph.model

import com.intellij.psi.PsiElement

data class ApiEndpoint(
    val httpMethod: String,
    val path: String,
    val className: String,
    val methodName: String = "",
    val psiElement: PsiElement
)