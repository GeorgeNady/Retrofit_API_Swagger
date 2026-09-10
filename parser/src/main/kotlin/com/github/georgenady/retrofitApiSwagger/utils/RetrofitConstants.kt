package com.github.georgenady.retrofitApiSwagger.utils

/**
 * Constants used during Retrofit annotation discovery and parsing.
 */
object RetrofitConstants {
    /**
     * Standard HTTP methods supported by Retrofit annotations.
     */
    val HTTP_METHODS = setOf(
        "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "HTTP"
    )

    /**
     * Common Retrofit package prefix for HTTP method annotations.
     */
    const val RETROFIT_PACKAGE_PREFIX = "retrofit2.http."

    /**
     * Annotation name for cache support tagging.
     */
    @Deprecated("will be removed in a future versions")
    const val SUPPORT_CACHE = "SupportCache"
}