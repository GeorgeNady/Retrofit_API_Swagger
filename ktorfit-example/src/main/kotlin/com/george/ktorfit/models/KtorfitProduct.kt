package com.george.ktorfit.models

data class KtorfitProduct(
    val id: Int = 0,
    val title: String,
    val price: Double,
    val description: String? = null,
    val category: String? = null
)
