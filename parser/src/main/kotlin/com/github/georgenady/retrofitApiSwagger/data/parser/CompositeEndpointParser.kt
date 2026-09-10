package com.github.georgenady.retrofitApiSwagger.data.parser

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode
import com.intellij.psi.PsiFile

/**
 * Composite parser that aggregates multiple [FileEndpointParser] implementations
 * and delegates parsing to the first matching parser for a given file.
 *
 * @property parsers The list of supported file endpoint parsers. Defaults to Kotlin and Java parsers.
 */
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
