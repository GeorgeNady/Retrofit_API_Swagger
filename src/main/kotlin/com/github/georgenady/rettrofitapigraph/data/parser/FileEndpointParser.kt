package com.github.georgenady.rettrofitapigraph.data.parser

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.intellij.psi.PsiFile

interface FileEndpointParser {
    fun canParse(psiFile: PsiFile): Boolean
    fun parse(psiFile: PsiFile): List<ApiNode>
}
