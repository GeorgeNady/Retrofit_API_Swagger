package com.github.georgenady.rettrofitapigraph.model

import com.intellij.psi.PsiElement

data class ApiNode(
    val methodName: String,
    val httpMethod: String, // GET, POST, etc.
    val path: String,
    val className: String,
    val psiElement: PsiElement? = null
)
