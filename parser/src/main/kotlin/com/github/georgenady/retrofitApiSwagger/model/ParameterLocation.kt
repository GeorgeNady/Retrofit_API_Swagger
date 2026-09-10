package com.github.georgenady.retrofitApiSwagger.model

/**
 * Defines the HTTP transmission location of a parameter in a Retrofit endpoint declaration.
 */
enum class ParameterLocation {
    PATH,
    QUERY,
    HEADER,
    BODY
}
