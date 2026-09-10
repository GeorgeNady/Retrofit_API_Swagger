package com.github.georgenady.retrofitApiSwagger.domain.usecase

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiFilterModel
import com.github.georgenady.retrofitApiSwagger.domain.model.ApiNode

class FilterEndpointsUseCase {
    operator fun invoke(endpoints: List<ApiNode>, filter: ApiFilterModel): List<ApiNode> {
        return endpoints.filter { filter.matches(it) }
    }
}
