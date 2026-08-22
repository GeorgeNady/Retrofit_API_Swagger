package com.github.georgenady.rettrofitapigraph.domain.usecase

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode

class GroupEndpointsByServiceUseCase {
    operator fun invoke(endpoints: List<ApiNode>): Map<String, List<ApiNode>> {
        return endpoints.groupBy { it.className }
    }
}
