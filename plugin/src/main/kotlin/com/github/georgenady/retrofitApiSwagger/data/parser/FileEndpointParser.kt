package com.github.georgenady.retrofitApiSwagger.data.parser

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode
import com.intellij.psi.PsiFile

interface FileEndpointParser {
    fun canParse(psiFile: PsiFile): Boolean
    fun parse(psiFile: PsiFile): List<ApiNode>
}
