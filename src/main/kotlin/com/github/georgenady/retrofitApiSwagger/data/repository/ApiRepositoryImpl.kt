package com.github.georgenady.retrofitApiSwagger.data.repository

import com.github.georgenady.retrofitApiSwagger.data.collector.ProjectSourceFileCollector
import com.github.georgenady.retrofitApiSwagger.data.parser.CompositeEndpointParser
import com.github.georgenady.retrofitApiSwagger.data.parser.FileEndpointParser
import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.domain.model.ScanOperation
import com.github.georgenady.retrofitApiSwagger.domain.model.ScanResult
import com.github.georgenady.retrofitApiSwagger.domain.repository.ApiRepository
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield

@Service(Service.Level.PROJECT)
class ApiRepositoryImpl(
    private val project: Project
) : ApiRepository {

    private val fileCollector = ProjectSourceFileCollector(project)
    private val endpointParser: FileEndpointParser = CompositeEndpointParser()

    override fun scanEndpoints(): Flow<ScanOperation> = flow {
        emit(ScanOperation.Started)
        
        val startTime = System.currentTimeMillis()
        
        if (DumbService.isDumb(project)) {
            emit(ScanOperation.Completed(ScanResult(emptyList(), 0, 0, true)))
            return@flow
        }

        val filesToScan = fileCollector.collectSourceFiles()
        val totalFilesCount = filesToScan.size
        
        val endpoints = mutableListOf<ApiNode>()
        val psiManager = PsiManager.getInstance(project)

        for ((index, virtualFile) in filesToScan.withIndex()) {
            yield() // Cooperative cancellation
            
            val fraction = if (totalFilesCount > 0) (index.toDouble() / totalFilesCount) else 1.0
            emit(ScanOperation.InProgress(fraction, virtualFile.name, index + 1, totalFilesCount))

            val fileEndpoints = readAction {
                if (!virtualFile.isValid) return@readAction emptyList()
                val psiFile = psiManager.findFile(virtualFile) ?: return@readAction emptyList()
                endpointParser.parse(psiFile)
            }

            endpoints.addAll(fileEndpoints)
        }

        val duration = System.currentTimeMillis() - startTime
        emit(ScanOperation.Completed(ScanResult(endpoints, totalFilesCount, duration, false)))
    }

    override fun findRetrofitEndpointsInFile(virtualFile: VirtualFile): List<ApiNode> {
        if (DumbService.isDumb(project)) return emptyList()

        return runReadAction {
            if (!virtualFile.isValid) return@runReadAction emptyList()
            val psiManager = PsiManager.getInstance(project)
            val psiFile = psiManager.findFile(virtualFile) ?: return@runReadAction emptyList()
            endpointParser.parse(psiFile)
        }
    }
}
