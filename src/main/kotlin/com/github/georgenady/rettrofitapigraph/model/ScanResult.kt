package com.github.georgenady.rettrofitapigraph.model

data class ScanResult(
    val endpoints: List<ApiNode>,
    val filesScanned: Int,
    val durationMs: Long,
    val isDumb: Boolean
)