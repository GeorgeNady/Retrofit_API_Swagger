package com.github.georgenady.rettrofitapigraph.data.collector

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.idea.KotlinFileType

class ProjectSourceFileCollector(private val project: Project) {

    fun collectSourceFiles(indicator: ProgressIndicator? = null): List<VirtualFile> {
        return runReadAction {
            val scope = ProjectScope.getContentScope(project)
            val fileIndex = ProjectRootManager.getInstance(project).fileIndex
            val processedFiles = LinkedHashSet<VirtualFile>()

            indicator?.checkCanceled()
            val ktFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope)
            for (file in ktFiles) {
                if (file.isValid && fileIndex.isInSourceContent(file)) {
                    processedFiles.add(file)
                }
            }

            indicator?.checkCanceled()
            val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope)
            for (file in javaFiles) {
                if (file.isValid && fileIndex.isInSourceContent(file)) {
                    processedFiles.add(file)
                }
            }

            if (processedFiles.isEmpty()) {
                indicator?.checkCanceled()
                thisLogger().info("Index results empty, walking module source roots.")
                val modules = ModuleManager.getInstance(project).modules
                for (module in modules) {
                    indicator?.checkCanceled()
                    val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots
                    for (root in sourceRoots) {
                        VfsUtilCore.iterateChildrenRecursively(root, null) { vf ->
                            indicator?.checkCanceled()
                            if (!vf.isDirectory && (vf.extension == "kt" || vf.extension == "java")) {
                                if (vf.isValid && fileIndex.isInSourceContent(vf)) {
                                    processedFiles.add(vf)
                                }
                            }
                            true
                        }
                    }
                }
            }

            processedFiles.toList()
        }
    }
}
