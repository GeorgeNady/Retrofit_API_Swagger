package com.george.example.service

import com.george.example.models.request.DummyProductRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DummyJsonService {

    // Get a single product (tests standard GET and path variables)
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): String

    // Search for products (tests query parameters)
    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): String

    // Add a new product (tests body schema generation)
    // Note: DummyJSON will simulate adding it and return the ID, 
    // but won't actually mutate the public database.
    @POST("products/add")
    suspend fun addProduct(@Body product: DummyProductRequest): String
    
    // Partially update a product
    @PATCH("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int, 
        @Body updates: Map<String, String>
    ): String
}