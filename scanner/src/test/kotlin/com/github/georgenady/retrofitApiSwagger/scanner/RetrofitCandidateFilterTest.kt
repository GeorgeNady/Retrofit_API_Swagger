package com.github.georgenady.retrofitApiSwagger.scanner

import com.github.georgenady.retrofitApiSwagger.scanner.filter.RetrofitCandidateFilter
import com.github.georgenady.retrofitApiSwagger.scanner.filter.impl.RetrofitCandidateFilterImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.idea.KotlinFileType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RetrofitCandidateFilterTest : BasePlatformTestCase() {

    @Test
    fun testCandidateFilterIdentifiesRetrofitFile() {
        val retrofitFile = myFixture.configureByText(
            KotlinFileType.INSTANCE,
            """
                package com.example
                import retrofit2.http.GET
                interface UserApi {
                    @GET("users")
                    fun getUsers(): List<String>
                }
            """.trimIndent()
        ).virtualFile

        val nonRetrofitFile = myFixture.configureByText(
            "Helper.kt",
            """
                package com.example
                class Helper {
                    fun doSomething() = 42
                }
            """.trimIndent()
        ).virtualFile

        val filter = RetrofitCandidateFilterImpl()

        assertTrue(filter.isCandidate(retrofitFile))
        assertFalse(filter.isCandidate(nonRetrofitFile))
    }
}
