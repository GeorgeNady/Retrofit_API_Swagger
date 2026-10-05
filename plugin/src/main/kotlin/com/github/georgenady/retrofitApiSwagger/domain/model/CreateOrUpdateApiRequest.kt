package com.github.georgenady.retrofitApiSwagger.domain.model

data class CreateOrUpdateApiRequest(
    val isUpdate: Boolean,
    val originalSignature: String?,
    val httpMethod: String,
    val path: String,
    val methodName: String,
    val returnType: String,
    val isSuspend: Boolean = true,
    val parameters: List<ApiParamPayload> = emptyList()
)
