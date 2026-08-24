package com.george.example.models.request

data class DummyProductRequest(
    val title: String,
    val description: String,
    val price: Double,
    val category: String
)