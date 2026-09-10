package com.github.georgenady.retrofitApiSwagger.domain.repository

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.domain.model.ScanOperation
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.flow.Flow

interface ApiRepository {
    fun scanEndpoints(): Flow<ScanOperation>
    fun findRetrofitEndpointsInFile(virtualFile: VirtualFile): List<ApiNode>
}
