package com.github.georgenady.retrofitApiSwagger.domain.usecase

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode

class GroupEndpointsByServiceUseCase {
    operator fun invoke(endpoints: List<ApiNode>): Map<String, List<ApiNode>> {
        return endpoints.groupBy { it.className }
    }
}
