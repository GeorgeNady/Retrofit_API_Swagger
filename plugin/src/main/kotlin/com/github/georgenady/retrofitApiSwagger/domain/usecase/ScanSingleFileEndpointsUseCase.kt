package com.github.georgenady.retrofitApiSwagger.domain.usecase

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.domain.repository.ApiRepository
import com.intellij.openapi.vfs.VirtualFile

class ScanSingleFileEndpointsUseCase(private val repository: ApiRepository) {
    operator fun invoke(file: VirtualFile): List<ApiNode> {
        return repository.findRetrofitEndpointsInFile(file)
    }
}
