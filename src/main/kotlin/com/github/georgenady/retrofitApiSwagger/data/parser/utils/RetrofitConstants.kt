package com.github.georgenady.retrofitApiSwagger.data.parser.utils

object RetrofitConstants {
    val HTTP_METHODS = setOf(
        "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "HTTP"
    )
    const val RETROFIT_PACKAGE_PREFIX = "retrofit2.http."
    const val SUPPORT_CACHE = "SupportCache"
}