package com.github.georgenady.retrofitApiSwagger.domain.edgeaction.engine

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateEvaluationEngineTest {

    private val engine = TemplateEvaluationEngine()

    private val sourceNode = ApiNode(
        methodName = "getUserDetails",
        httpMethod = "GET",
        path = "/users/{id}",
        className = "UserService",
        returnTypeFqn = "com.example.UserResponse"
    )

    private val targetNode = ApiNode(
        methodName = "updateUser",
        httpMethod = "POST",
        path = "/users/update",
        className = "UserService",
        returnTypeFqn = "Unit"
    )

    @Test
    fun `test basic variable evaluation`() {
        val template = "@InvalidateCache(keys = [PreferenceKey.\${source.methodName.toUpperCase()}_CACHE_KEY])"
        val result = engine.evaluate(template, sourceNode, targetNode)

        assertEquals(
            "@InvalidateCache(keys = [PreferenceKey.GETUSERDETAILS_CACHE_KEY])",
            result
        )
    }

    @Test
    fun `test flow chain comment evaluation`() {
        val template = "// Chained: \${source.name}() -> \${target.name}() on \${target.path}"
        val result = engine.evaluate(template, sourceNode, targetNode)

        assertEquals(
            "// Chained: getUserDetails() -> updateUser() on /users/update",
            result
        )
    }

    @Test
    fun `test lowercase and uppercase transformation`() {
        val template = "\${source.name.uppercase()} and \${target.httpMethod.lowercase()}"
        val result = engine.evaluate(template, sourceNode, targetNode)

        assertEquals(
            "GETUSERDETAILS and post",
            result
        )
    }
}
