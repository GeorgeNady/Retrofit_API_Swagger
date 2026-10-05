package com.george.ktorfit.service

import com.george.ktorfit.models.KtorfitProduct
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface KtorfitDummyJsonService {

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): String

    // Get a single product by ID
    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int,
        @Header("Accept") accept: String = "application/json"
    ): String

    // Search for products by query string
    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): String

    // Add a new product with body payload
    @POST("products/add")
    suspend fun addProduct(
        @Body product: KtorfitProduct
    ): String

    // Replace product completely
    @PUT("products/{id}")
    suspend fun replaceProduct(
        @Path("id") id: Int,
        @Body product: KtorfitProduct
    ): String

    // Partially update a product
    @PATCH("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body updates: Map<String, String>
    ): String

    // Delete a product
    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int
    ): String
}
