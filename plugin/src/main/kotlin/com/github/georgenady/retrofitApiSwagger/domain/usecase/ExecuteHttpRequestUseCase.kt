package com.github.georgenady.retrofitApiSwagger.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class ExecuteHttpRequestUseCase {
    suspend fun invoke(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?
    ): Result<String> {
        return try {
            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()

            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            when (method.uppercase()) {
                "GET" -> requestBuilder.GET()
                "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body ?: ""))
                "PUT" -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body ?: ""))
                "DELETE" -> requestBuilder.DELETE()
                else -> requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(body ?: ""))
            }

            val response = withContext(Dispatchers.IO) {
                client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
