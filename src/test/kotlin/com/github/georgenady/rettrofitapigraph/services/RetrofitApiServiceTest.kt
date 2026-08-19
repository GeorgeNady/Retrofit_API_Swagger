package com.github.georgenady.rettrofitapigraph.services

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.parser.CompositeEndpointParser
import com.github.georgenady.rettrofitapigraph.parser.JavaEndpointParser
import com.github.georgenady.rettrofitapigraph.parser.KotlinEndpointParser
import com.github.georgenady.rettrofitapigraph.toolWindow.MyToolWindow
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RetrofitApiServiceTest : BasePlatformTestCase() {

    @Test
    fun testScanKotlinFileWithRetrofitAnnotations() {
        val kotlinCode = """
            package com.example.api

            import retrofit2.http.GET
            import retrofit2.http.POST
            import retrofit2.http.PUT
            import retrofit2.http.DELETE
            import retrofit2.http.HTTP
            import retrofit2.http.Path
            import retrofit2.http.Body
            import com.example.annotations.SupportCache

            interface UserApiService {
                @GET("users/{id}")
                @SupportCache
                suspend fun getUser(@Path("id") id: String): User

                @POST("users/create")
                suspend fun createUser(@Body user: User): Response

                @PUT("users/update")
                fun updateUser(@Body user: User): Response

                @DELETE("users/{id}")
                fun deleteUser(@Path("id") id: String)

                @HTTP(method = "CUSTOM", path = "users/custom")
                fun customHttp()
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project).createFileFromText(
            "UserApiService.kt",
            KotlinFileType.INSTANCE,
            kotlinCode
        ) as KtFile

        val service = RetrofitApiService(project)
        val endpoints = mutableListOf<ApiNode>()
        service.scanKotlinFile(psiFile, endpoints)

        assertEquals(5, endpoints.size)

        val getEndpoint = endpoints.find { it.methodName == "getUser" }
        assertNotNull(getEndpoint)
        assertEquals("GET", getEndpoint?.httpMethod)
        assertEquals("users/{id}", getEndpoint?.path)
        assertEquals("UserApiService", getEndpoint?.className)
        assertTrue(getEndpoint?.supportsCache == true)

        val postEndpoint = endpoints.find { it.methodName == "createUser" }
        assertNotNull(postEndpoint)
        assertEquals("POST", postEndpoint?.httpMethod)
        assertEquals("users/create", postEndpoint?.path)

        val putEndpoint = endpoints.find { it.methodName == "updateUser" }
        assertNotNull(putEndpoint)
        assertEquals("PUT", putEndpoint?.httpMethod)
        assertEquals("users/update", putEndpoint?.path)

        val deleteEndpoint = endpoints.find { it.methodName == "deleteUser" }
        assertNotNull(deleteEndpoint)
        assertEquals("DELETE", deleteEndpoint?.httpMethod)
        assertEquals("users/{id}", deleteEndpoint?.path)

        val httpEndpoint = endpoints.find { it.methodName == "customHttp" }
        assertNotNull(httpEndpoint)
        assertEquals("HTTP", httpEndpoint?.httpMethod)
        assertEquals("users/custom", httpEndpoint?.path)
    }

    @Test
    fun testScanJavaFileWithRetrofitAnnotations() {
        val javaCode = """
            package com.example.api;

            import retrofit2.http.GET;
            import retrofit2.http.POST;
            import retrofit2.http.HTTP;
            import retrofit2.Call;

            public interface JavaApiService {
                @GET("items")
                Call<List<Item>> getItems();

                @POST("items/add")
                Call<Void> addItem();

                @HTTP(method = "DELETE", path = "items/remove")
                Call<Void> removeItem();
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project).createFileFromText(
            "JavaApiService.java",
            JavaFileType.INSTANCE,
            javaCode
        ) as PsiJavaFile

        val service = RetrofitApiService(project)
        val endpoints = mutableListOf<ApiNode>()
        service.scanJavaFile(psiFile, endpoints)

        assertEquals(3, endpoints.size)

        val getItems = endpoints.find { it.methodName == "getItems" }
        assertNotNull(getItems)
        assertEquals("GET", getItems?.httpMethod)
        assertEquals("items", getItems?.path)
        assertEquals("JavaApiService", getItems?.className)

        val addItem = endpoints.find { it.methodName == "addItem" }
        assertNotNull(addItem)
        assertEquals("POST", addItem?.httpMethod)
        assertEquals("items/add", addItem?.path)

        val removeItem = endpoints.find { it.methodName == "removeItem" }
        assertNotNull(removeItem)
        assertEquals("HTTP", removeItem?.httpMethod)
        assertEquals("items/remove", removeItem?.path)
    }

    @Test
    fun testCompositeEndpointParser() {
        val composite = CompositeEndpointParser()

        val kotlinFile = PsiFileFactory.getInstance(project).createFileFromText(
            "Sample.kt",
            KotlinFileType.INSTANCE,
            """
                interface SampleApi {
                    @retrofit2.http.GET("test")
                    fun test(): String
                }
            """.trimIndent()
        )

        assertTrue(composite.canParse(kotlinFile))
        val parsed = composite.parse(kotlinFile)
        assertEquals(1, parsed.size)
        assertEquals("GET", parsed[0].httpMethod)
        assertEquals("test", parsed[0].path)
    }

    @Test
    fun testRetrofitApiServiceServiceResolution() {
        val constructor = RetrofitApiService::class.java.getDeclaredConstructor(Project::class.java)
        assertNotNull(constructor)
        val serviceInstance = project.service<RetrofitApiService>()
        assertNotNull(serviceInstance)
    }

    @Test
    fun testMyToolWindowCreation() {
        val toolWindow = MyToolWindow(project)
        assertNotNull(toolWindow.getComponent())
    }
}
