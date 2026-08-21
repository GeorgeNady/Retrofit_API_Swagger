package com.github.georgenady.rettrofitapigraph.data.parser

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.intellij.psi.PsiFile

class CompositeEndpointParser(
    private val parsers: List<FileEndpointParser> = listOf(
        KotlinEndpointParser(),
        JavaEndpointParser()
    )
) : FileEndpointParser {

    override fun canParse(psiFile: PsiFile): Boolean {
        return parsers.any { it.canParse(psiFile) }
    }

    override fun parse(psiFile: PsiFile): List<ApiNode> {
        val parser = parsers.firstOrNull { it.canParse(psiFile) } ?: return emptyList()
        return parser.parse(psiFile)
    }
}
