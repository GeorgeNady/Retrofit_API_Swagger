package com.github.georgenady.retrofitApiSwagger.domain.model

import com.github.georgenady.retrofitApiSwagger.model.ApiNode

data class ScanResult(
    val endpoints: List<ApiNode>,
    val filesScanned: Int,
    val durationMs: Long,
    val isDumb: Boolean
)