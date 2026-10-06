package com.github.georgenady.retrofitApiSwagger.parser.impl

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.parser.FileEndpointParser
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Composite parser that aggregates multiple [com.github.georgenady.retrofitApiSwagger.parser.FileEndpointParser] implementations
 * and delegates parsing to the first matching parser for a given file.
 *
 * @property parsers The list of supported file endpoint parsers. Defaults to Kotlin and Java parsers.
 */
@Service(Service.Level.PROJECT)
internal class CompositeEndpointParser(
    private val parsers: List<FileEndpointParser> = listOf(
        KotlinEndpointParser(),
        JavaEndpointParser()
    )
) : FileEndpointParser {

    constructor(project: Project) : this(
        listOf(
            KotlinEndpointParser(),
            JavaEndpointParser()
        )
    )

    override fun canParse(psiFile: PsiFile): Boolean {
        return parsers.any { it.canParse(psiFile) }
    }

    override fun parse(psiFile: PsiFile): List<ApiNode> {
        val parser = parsers.firstOrNull { it.canParse(psiFile) } ?: return emptyList()
        return parser.parse(psiFile)
    }
}
