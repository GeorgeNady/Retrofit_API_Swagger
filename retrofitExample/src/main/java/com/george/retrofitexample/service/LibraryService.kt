package com.george.retrofitexample.service

import com.george.retrofitexample.models.request.BookDepositRequest
import com.george.retrofitexample.models.response.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.OPTIONS
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryService {

    @GET("api/v1/health/ping")
    suspend fun pingServer(): ResponseBody

    @GET("api/v1/users/{userId}")
    suspend fun getUserDetails(
        @Path("userId") userId: String,
        @Query("includeHistory") includeHistory: Boolean
    ): UserResponse

    @POST("api/v1/books/deposit")
    suspend fun submitBookDeposit(
        @Header("Authorization") token: String,
        @Body request: BookDepositRequest
    ): String

    @Multipart
    @POST("api/v1/users/{userId}/avatar")
    suspend fun uploadAvatar(
        @Path("userId") userId: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): ResponseBody

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): UserResponse

    @PUT("api/v1/books/{bookId}/status")
    suspend fun updateBookStatus(
        @Path("bookId") bookId: String,
        @Body status: String
    )

    @PATCH("api/v1/books/{bookId}")
    suspend fun updateBookNotes(
        @Path("bookId") bookId: String,
        @Body updates: Map<String, String>
    ): BookDepositRequest

    @DELETE("api/v1/users/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String
    )

    @HEAD("api/v1/system/status")
    suspend fun checkSystemStatus(): Void

    @OPTIONS("api/v1/users")
    suspend fun getUserOptions(): ResponseBody
}