package com.github.georgenady.retrofitApiSwagger.data.repository

import com.github.georgenady.retrofitApiSwagger.domain.repository.ApiRepository
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for ApiRepository service resolution and repository operations.
 */
@RunWith(JUnit4::class)
class ApiRepositoryImplTest : BasePlatformTestCase() {

    @Test
    fun testApiRepositoryServiceResolution() {
        val serviceInstance = project.service<ApiRepository>()
        assertNotNull(serviceInstance)
    }
}
