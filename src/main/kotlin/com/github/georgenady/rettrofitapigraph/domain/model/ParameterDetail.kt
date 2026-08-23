package com.github.georgenady.rettrofitapigraph.domain.model

enum class ParameterLocation {
    PATH, QUERY, HEADER, BODY
}

data class ParameterDetail(
    val name: String,
    val type: String,
    val location: ParameterLocation = ParameterLocation.QUERY,
    val fqn: String? = null,
    val defaultValue: String? = null
)
