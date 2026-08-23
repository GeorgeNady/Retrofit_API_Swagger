package com.github.georgenady.rettrofitapigraph.domain.model

import com.intellij.openapi.vfs.VirtualFile

sealed class ScanOperation {
    object Started : ScanOperation()
    data class InProgress(
        val fraction: Double,
        val currentFile: String,
        val currentCount: Int,
        val totalCount: Int
    ) : ScanOperation()
    data class Completed(val result: ScanResult) : ScanOperation()
    data class Failed(val throwable: Throwable) : ScanOperation()
}
