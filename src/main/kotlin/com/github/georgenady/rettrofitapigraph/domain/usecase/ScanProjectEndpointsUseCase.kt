package com.github.georgenady.rettrofitapigraph.domain.usecase

import com.github.georgenady.rettrofitapigraph.domain.model.ScanOperation
import com.github.georgenady.rettrofitapigraph.domain.repository.ApiRepository
import kotlinx.coroutines.flow.Flow

class ScanProjectEndpointsUseCase(private val repository: ApiRepository) {
    operator fun invoke(): Flow<ScanOperation> {
        return repository.scanEndpoints()
    }
}
