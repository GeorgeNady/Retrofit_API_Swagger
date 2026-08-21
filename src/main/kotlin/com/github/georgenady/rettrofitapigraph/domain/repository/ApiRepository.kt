package com.github.georgenady.rettrofitapigraph.domain.repository

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.domain.model.ScanOperation
import com.github.georgenady.rettrofitapigraph.domain.model.ScanResult
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.flow.Flow

interface ApiRepository {
    fun scanEndpoints(): Flow<ScanOperation>
    fun findRetrofitEndpointsInFile(virtualFile: VirtualFile): List<ApiNode>
}
