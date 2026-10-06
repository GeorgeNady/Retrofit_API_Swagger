package com.github.georgenady.retrofitApiSwagger.model

/**
 * Encapsulates an annotation name and its key-value arguments extracted from source code.
 *
 * @property name The short name or simple name of the annotation.
 * @property arguments Key-value map of arguments supplied to the annotation.
 */
data class AnnotationDetail(
    val name: String,
    val arguments: Map<String, String>
)
