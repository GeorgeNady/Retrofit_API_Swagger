package com.github.georgenady.retrofitApiSwagger.parser

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.intellij.psi.PsiFile

/**
 * Strategy interface for parsing Retrofit API endpoints from source code PSI files.
 */
interface FileEndpointParser {
    /**
     * Checks if this parser supports the given PSI file (e.g. by file language or type).
     *
     * @param psiFile The source PSI file to check.
     * @return True if the file can be processed by this parser.
     */
    fun canParse(psiFile: PsiFile): Boolean

    /**
     * Parses the PSI file and extracts all declared Retrofit API endpoints.
     *
     * @param psiFile The source PSI file containing endpoint declarations.
     * @return A list of discovered [ApiNode] items.
     */
    fun parse(psiFile: PsiFile): List<ApiNode>
}
