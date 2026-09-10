package com.github.georgenady.retrofitApiSwagger.scanner.collector.impl

import com.github.georgenady.retrofitApiSwagger.scanner.collector.ProjectSourceFileCollector
import com.github.georgenady.retrofitApiSwagger.scanner.settings.ScannerSettings
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.idea.KotlinFileType

/**
 * Concrete implementation of [ProjectSourceFileCollector] using IntelliJ's [FileTypeIndex]
 * with a fallback recursive content-root traversal.
 */
@Service(Service.Level.PROJECT)
internal class ProjectSourceFileCollectorImpl(
    private val project: Project
) : ProjectSourceFileCollector {

    override fun collectSourceFiles(indicator: ProgressIndicator?): List<VirtualFile> {
        return runReadAction {
            val scope = ProjectScope.getContentScope(project)
            val fileIndex = ProjectRootManager.getInstance(project).fileIndex
            val processedFiles = LinkedHashSet<VirtualFile>()

            val scannerSettings = project.getService(ScannerSettings::class.java)
            val scanKotlin = scannerSettings?.isKotlinEnabled ?: true
            val scanJava = scannerSettings?.isJavaEnabled ?: true

            val supportedFileTypes = mutableListOf<FileType>()
            if (scanKotlin) {
                supportedFileTypes.add(KotlinFileType.INSTANCE)
            }
            if (scanJava) {
                supportedFileTypes.add(JavaFileType.INSTANCE)
            }

            for (fileType in supportedFileTypes) {
                processedFiles.addAll(collectIndexedFiles(fileType, scope, fileIndex, indicator))
            }

            if (processedFiles.isEmpty()) {
                indicator?.checkCanceled()
                thisLogger().info("Index results empty, walking project content roots.")
                val contentRoots = ProjectRootManager.getInstance(project).contentRoots
                for (root in contentRoots) {
                    indicator?.checkCanceled()
                    VfsUtilCore.iterateChildrenRecursively(root, { dir ->
                        !fileIndex.isExcluded(dir)
                    }) { vf ->
                        indicator?.checkCanceled()
                        if (!vf.isDirectory) {
                            val matchesExtension = (scanKotlin && vf.extension == "kt") || (scanJava && vf.extension == "java")
                            if (matchesExtension && isEligibleSourceFile(vf, fileIndex)) {
                                processedFiles.add(vf)
                            }
                        }
                        true
                    }
                }
            }

            processedFiles.toList()
        }
    }

    /**
     * Queries indexed files of the specified [fileType] and filters for valid, non-excluded files.
     */
    private fun collectIndexedFiles(
        fileType: FileType,
        scope: GlobalSearchScope,
        fileIndex: ProjectFileIndex,
        indicator: ProgressIndicator?
    ): List<VirtualFile> {
        indicator?.checkCanceled()
        val files = FileTypeIndex.getFiles(fileType, scope)
        return files.filter { isEligibleSourceFile(it, fileIndex) }
    }

    /**
     * Verifies that a [VirtualFile] is valid and not part of an excluded directory (e.g. build output).
     */
    private fun isEligibleSourceFile(file: VirtualFile, fileIndex: ProjectFileIndex): Boolean {
        return file.isValid && !fileIndex.isExcluded(file)
    }
}
