package com.github.georgenady.retrofitApiSwagger.scanner

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.scanner.cache.EndpointScanCache
import com.github.georgenady.retrofitApiSwagger.scanner.cache.impl.EndpointScanCacheImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.idea.KotlinFileType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EndpointScanCacheTest : BasePlatformTestCase() {

    @Test
    fun testCacheHitAndInvalidation() {
        val psiFile = myFixture.configureByText(
            KotlinFileType.INSTANCE,
            """
                package com.example
                interface Api
            """.trimIndent()
        )
        val virtualFile = psiFile.virtualFile

        val cache = EndpointScanCacheImpl(project)

        // Initial check: miss
        assertNull(cache.get(virtualFile))

        val dummyEndpoint = ApiNode(
            className = "Api",
            methodName = "getData",
            httpMethod = "GET",
            path = "api/data"
        )

        // Store
        cache.put(virtualFile, listOf(dummyEndpoint))

        // Hit
        val cached = cache.get(virtualFile)
        assertNotNull(cached)
        assertEquals(1, cached?.size)
        assertEquals("getData", cached?.first()?.methodName)

        // Invalidate
        cache.invalidate(virtualFile)
        assertNull(cache.get(virtualFile))
    }
}
