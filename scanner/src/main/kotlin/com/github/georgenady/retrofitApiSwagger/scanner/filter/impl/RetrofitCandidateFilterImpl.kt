package com.github.georgenady.retrofitApiSwagger.scanner.filter.impl

import com.github.georgenady.retrofitApiSwagger.scanner.filter.RetrofitCandidateFilter
import com.intellij.openapi.components.Service
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

/**
 * Concrete implementation of [RetrofitCandidateFilter] performing rapid text inspection.
 */
@Service(Service.Level.APP)
internal class RetrofitCandidateFilterImpl : RetrofitCandidateFilter {

    private val retrofitKeywords = listOf(
        "retrofit2",
        "@GET",
        "@POST",
        "@PUT",
        "@DELETE",
        "@PATCH",
        "@HEAD",
        "@OPTIONS",
        "@HTTP"
    )

    override fun isCandidate(virtualFile: VirtualFile): Boolean {
        if (!virtualFile.isValid || virtualFile.isDirectory) return false

        return try {
            val content = VfsUtilCore.loadText(virtualFile)
            retrofitKeywords.any { content.contains(it) } && content.contains(retrofitKeywords.first())
        } catch (_: Throwable) {
            // Fallback to true on read failure so normal parsing can attempt handling
            true
        }
    }
}
