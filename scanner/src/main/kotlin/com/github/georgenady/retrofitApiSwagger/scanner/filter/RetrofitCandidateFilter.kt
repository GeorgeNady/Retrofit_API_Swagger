package com.github.georgenady.retrofitApiSwagger.scanner.filter

import com.intellij.openapi.vfs.VirtualFile

/**
 * Fast pre-filter contract to determine whether a [VirtualFile] is a candidate
 * for containing Retrofit endpoint definitions without loading heavy AST / PSI.
 */
interface RetrofitCandidateFilter {

    /**
     * Checks if the file contains signature Retrofit keywords or annotations.
     *
     * @param virtualFile The file to inspect.
     * @return `true` if the file could contain Retrofit declarations; `false` otherwise.
     */
    fun isCandidate(virtualFile: VirtualFile): Boolean
}
