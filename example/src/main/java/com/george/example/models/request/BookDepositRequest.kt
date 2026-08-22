package com.george.example.models.request

data class BookDepositRequest(
    val bookId: String,
    val depositAmount: Double,
    val userNotes: String?
)

