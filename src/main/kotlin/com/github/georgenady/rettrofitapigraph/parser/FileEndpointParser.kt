package com.github.georgenady.rettrofitapigraph.parser

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.intellij.psi.PsiFile

interface FileEndpointParser {
    fun canParse(psiFile: PsiFile): Boolean
    fun parse(psiFile: PsiFile): List<ApiNode>
}
