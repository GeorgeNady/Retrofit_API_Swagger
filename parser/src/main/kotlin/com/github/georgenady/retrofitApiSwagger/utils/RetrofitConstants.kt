package com.github.georgenady.retrofitApiSwagger.utils

/**
 * Constants used during Retrofit and Ktorfit annotation discovery and parsing.
 */
object RetrofitConstants {
    /**
     * Standard HTTP methods supported by Retrofit and Ktorfit annotations.
     */
    val HTTP_METHODS = setOf(
        "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "HTTP"
    )

    /**
     * Common Retrofit package prefix for HTTP method annotations.
     */
    const val RETROFIT_PACKAGE_PREFIX = "retrofit2.http."

    /**
     * Common Ktorfit package prefix for HTTP method annotations.
     */
    const val KTORFIT_PACKAGE_PREFIX = "de.jensklingenberg.ktorfit.http."

    /**
     * Supported HTTP method package prefixes.
     */
    val PACKAGE_PREFIXES = listOf(
        RETROFIT_PACKAGE_PREFIX,
        KTORFIT_PACKAGE_PREFIX
    )

    /**
     * Annotation name for cache support tagging.
     */
    @Deprecated("will be removed in a future versions")
    const val SUPPORT_CACHE = "SupportCache"
}