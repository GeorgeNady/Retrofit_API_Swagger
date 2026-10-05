package com.github.georgenady.retrofitApiSwagger.scanner.filter.impl

import com.github.georgenady.retrofitApiSwagger.scanner.filter.RetrofitCandidateFilter
import com.intellij.openapi.components.Service
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

/**
 * Concrete implementation of [RetrofitCandidateFilter] performing rapid text inspection
 * for both Retrofit and Ktorfit endpoint declarations.
 */
@Service(Service.Level.APP)
internal class RetrofitCandidateFilterImpl : RetrofitCandidateFilter {

    private val httpKeywords = listOf(
        "@GET",
        "@POST",
        "@PUT",
        "@DELETE",
        "@PATCH",
        "@HEAD",
        "@OPTIONS",
        "@HTTP"
    )

    private val frameworkKeywords = listOf(
        "retrofit2",
        "retrofit",
        "ktorfit",
        "de.jensklingenberg.ktorfit"
    )

    override fun isCandidate(virtualFile: VirtualFile): Boolean {
        if (!virtualFile.isValid || virtualFile.isDirectory) return false

        return try {
            val content = VfsUtilCore.loadText(virtualFile)
            val hasFramework = frameworkKeywords.any { content.contains(it, ignoreCase = true) }
            val hasHttp = httpKeywords.any { content.contains(it) }
            hasFramework && hasHttp
        } catch (_: Throwable) {
            // Fallback to true on read failure so normal parsing can attempt handling
            true
        }
    }
}
