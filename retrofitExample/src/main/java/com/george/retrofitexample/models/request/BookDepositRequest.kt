package com.george.retrofitexample.models.request

data class BookDepositRequest(
    val bookId: String,
    val depositAmount: Double,
    val userNotes: String?
)

