package com.github.georgenady.retrofitApiSwagger.scanner

import com.github.georgenady.retrofitApiSwagger.scanner.collector.ProjectSourceFileCollector
import com.github.georgenady.retrofitApiSwagger.scanner.collector.impl.ProjectSourceFileCollectorImpl
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.idea.KotlinFileType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for [ProjectSourceFileCollector].
 */
@RunWith(JUnit4::class)
class ProjectSourceFileCollectorTest : BasePlatformTestCase() {

    @Test
    fun testCollectorImplementationCollectsFiles() {
        myFixture.configureByText(
            KotlinFileType.INSTANCE,
            """
                package com.example.test
                interface TestApi
            """.trimIndent()
        )

        myFixture.configureByText(
            JavaFileType.INSTANCE,
            """
                package com.example.test;
                public interface JavaApi {}
            """.trimIndent()
        )

        val collector: ProjectSourceFileCollector = ProjectSourceFileCollectorImpl(project)
        val files = collector.collectSourceFiles()

        assertNotNull(files)
        assertTrue(files.any { it.name.endsWith(".kt") })
        assertTrue(files.any { it.name.endsWith(".java") })
    }
}
