package com.github.georgenady.retrofitApiSwagger.domain.model

data class ScanResult(
    val endpoints: List<ApiNode>,
    val filesScanned: Int,
    val durationMs: Long,
    val isDumb: Boolean
)