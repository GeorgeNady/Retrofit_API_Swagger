package com.github.georgenady.retrofitApiSwagger.domain.model

/**
 * Represents metadata about an individual parameter of an API endpoint method.
 *
 * @property name The identifier name of the parameter.
 * @property type The string representation of the parameter type.
 * @property location The HTTP parameter transmission location (PATH, QUERY, HEADER, BODY).
 * @property fqn The fully-qualified type name, if resolvable.
 * @property defaultValue Optional default value assigned to the parameter.
 */
data class ParameterDetail(
    val name: String,
    val type: String,
    val location: ParameterLocation = ParameterLocation.QUERY,
    val fqn: String? = null,
    val defaultValue: String? = null
)
