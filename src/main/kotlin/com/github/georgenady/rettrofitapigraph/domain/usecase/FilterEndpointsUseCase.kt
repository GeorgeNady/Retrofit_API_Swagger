package com.github.georgenady.rettrofitapigraph.domain.usecase

import com.github.georgenady.rettrofitapigraph.domain.model.ApiFilterModel
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode

class FilterEndpointsUseCase {
    operator fun invoke(endpoints: List<ApiNode>, filter: ApiFilterModel): List<ApiNode> {
        return endpoints.filter { filter.matches(it) }
    }
}
