package com.github.georgenady.retrofitApiSwagger.domain.usecase

import com.github.georgenady.retrofitApiSwagger.domain.model.ScanOperation
import com.github.georgenady.retrofitApiSwagger.domain.repository.ApiRepository
import kotlinx.coroutines.flow.Flow

class ScanProjectEndpointsUseCase(private val repository: ApiRepository) {
    operator fun invoke(): Flow<ScanOperation> {
        return repository.scanEndpoints()
    }
}
