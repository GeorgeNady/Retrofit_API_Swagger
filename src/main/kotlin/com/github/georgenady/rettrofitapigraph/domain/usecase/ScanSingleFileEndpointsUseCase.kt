package com.github.georgenady.rettrofitapigraph.domain.usecase

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.domain.repository.ApiRepository
import com.intellij.openapi.vfs.VirtualFile

class ScanSingleFileEndpointsUseCase(private val repository: ApiRepository) {
    operator fun invoke(file: VirtualFile): List<ApiNode> {
        return repository.findRetrofitEndpointsInFile(file)
    }
}
