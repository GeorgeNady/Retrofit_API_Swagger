package com.github.georgenady.retrofitApiSwagger.data.repository

import com.github.georgenady.retrofitApiSwagger.scanner.collector.ProjectSourceFileCollector
import com.github.georgenady.retrofitApiSwagger.scanner.cache.EndpointScanCache
import com.github.georgenady.retrofitApiSwagger.scanner.filter.RetrofitCandidateFilter
import com.github.georgenady.retrofitApiSwagger.parser.FileEndpointParser
import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.domain.model.ScanOperation
import com.github.georgenady.retrofitApiSwagger.domain.model.ScanResult
import com.github.georgenady.retrofitApiSwagger.domain.repository.ApiRepository
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
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

    private val fileCollector: ProjectSourceFileCollector
        get() = project.getService(ProjectSourceFileCollector::class.java)

    private val endpointParser: FileEndpointParser
        get() = project.getService(FileEndpointParser::class.java)

    private val endpointCache: EndpointScanCache
        get() = project.getService(EndpointScanCache::class.java)

    private val candidateFilter: RetrofitCandidateFilter
        get() = ApplicationManager.getApplication().getService(RetrofitCandidateFilter::class.java)

    init {
        project.messageBus.connect().subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
            override fun exitDumbMode() {
                endpointCache.clear()
            }
        })
    }

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

            val cached = endpointCache.get(virtualFile)
            if (cached != null) {
                endpoints.addAll(cached)
                continue
            }

            if (!candidateFilter.isCandidate(virtualFile)) {
                endpointCache.put(virtualFile, emptyList())
                continue
            }

            val fileEndpoints = readAction {
                if (!virtualFile.isValid) return@readAction emptyList()
                val psiFile = psiManager.findFile(virtualFile) ?: return@readAction emptyList()
                try {
                    endpointParser.parse(psiFile)
                } catch (e: Throwable) {
                    thisLogger().warn("Failed to parse endpoints in ${virtualFile.path}", e)
                    emptyList()
                }
            }

            endpointCache.put(virtualFile, fileEndpoints)
            endpoints.addAll(fileEndpoints)
        }

        val duration = System.currentTimeMillis() - startTime
        emit(ScanOperation.Completed(ScanResult(endpoints, totalFilesCount, duration, false)))
    }

    override fun findRetrofitEndpointsInFile(virtualFile: VirtualFile): List<ApiNode> {
        if (DumbService.isDumb(project)) return emptyList()

        val cached = endpointCache.get(virtualFile)
        if (cached != null) return cached

        if (!candidateFilter.isCandidate(virtualFile)) {
            endpointCache.put(virtualFile, emptyList())
            return emptyList()
        }

        return runReadAction {
            if (!virtualFile.isValid) return@runReadAction emptyList()
            val psiManager = PsiManager.getInstance(project)
            val psiFile = psiManager.findFile(virtualFile) ?: return@runReadAction emptyList()
            val parsed = try {
                endpointParser.parse(psiFile)
            } catch (e: Throwable) {
                thisLogger().warn("Failed to parse endpoints in ${virtualFile.path}", e)
                emptyList()
            }
            if (parsed.isNotEmpty()) {
                endpointCache.put(virtualFile, parsed)
            }
            parsed
        }
    }
}
