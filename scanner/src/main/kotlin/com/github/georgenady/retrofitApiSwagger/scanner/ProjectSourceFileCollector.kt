package com.github.georgenady.retrofitApiSwagger.scanner

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vfs.VirtualFile

/**
 * Strategy contract for collecting source files within an IDE project to be scanned.
 */
interface ProjectSourceFileCollector {

    /**
     * Collects all candidate Kotlin and Java source files eligible for endpoint parsing.
     *
     * @param indicator Optional progress indicator to monitor cancellation and report steps.
     * @return List of matching project source files.
     */
    fun collectSourceFiles(indicator: ProgressIndicator? = null): List<VirtualFile>
}
