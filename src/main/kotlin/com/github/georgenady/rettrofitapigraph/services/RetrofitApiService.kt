package com.github.georgenady.rettrofitapigraph.services

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.model.AnnotationDetail
import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.model.ParameterDetail
import com.github.georgenady.rettrofitapigraph.model.ScanResult
import com.github.georgenady.rettrofitapigraph.parser.CompositeEndpointParser
import com.github.georgenady.rettrofitapigraph.parser.FileEndpointParser
import com.github.georgenady.rettrofitapigraph.parser.JavaEndpointParser
import com.github.georgenady.rettrofitapigraph.parser.KotlinEndpointParser
import com.github.georgenady.rettrofitapigraph.parser.RetrofitConstants
import com.github.georgenady.rettrofitapigraph.scanner.ProjectSourceFileCollector
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile

@Service(Service.Level.PROJECT)
class RetrofitApiService @JvmOverloads constructor(
    private val project: Project,
    private val fileCollector: ProjectSourceFileCollector = ProjectSourceFileCollector(project),
    private val endpointParser: FileEndpointParser = CompositeEndpointParser(),
    private val kotlinParser: KotlinEndpointParser = KotlinEndpointParser(),
    private val javaParser: JavaEndpointParser = JavaEndpointParser()
) {

    companion object {
        val HTTP_METHODS = RetrofitConstants.HTTP_METHODS
        const val RETROFIT_PACKAGE_PREFIX = RetrofitConstants.RETROFIT_PACKAGE_PREFIX
    }

    fun findRetrofitEndpoints(indicator: ProgressIndicator? = null): ScanResult {
        val startTime = System.currentTimeMillis()
        val isDumb = DumbService.isDumb(project)

        if (isDumb) {
            thisLogger().warn("🔄 " + MyBundle.message("dashboard.indexing"))
            return ScanResult(emptyList(), 0, 0, true)
        }

        indicator?.text = "🕵🏼‍♂️ " + MyBundle.message("dashboard.scanning")
        val filesToScan = fileCollector.collectSourceFiles(indicator)
        val totalFilesCount = filesToScan.size
        thisLogger().info("Collected $totalFilesCount project source files for scanning.")

        val endpoints = mutableListOf<ApiNode>()
        val psiManager = PsiManager.getInstance(project)

        for ((index, virtualFile) in filesToScan.withIndex()) {
            indicator?.checkCanceled()
            indicator?.fraction = if (totalFilesCount > 0) (index.toDouble() / totalFilesCount) else 1.0
            indicator?.text = "Scanning API endpoints 🔎 (${index + 1}/$totalFilesCount)..."
            indicator?.text2 = virtualFile.name

            val fileEndpoints = runReadActionBlocking {
                if (!virtualFile.isValid) return@runReadActionBlocking emptyList()
                val psiFile = psiManager.findFile(virtualFile) ?: return@runReadActionBlocking emptyList()
                val parsed = endpointParser.parse(psiFile)
                if (parsed.isNotEmpty()) {
                    thisLogger().info("Parsed ${parsed.size} endpoints from ${virtualFile.name}")
                }
                parsed
            }

            endpoints.addAll(fileEndpoints)
        }

        val duration = System.currentTimeMillis() - startTime
        thisLogger().info("✅ " + MyBundle.message("dashboard.found_endpoints", endpoints.size, duration))
        return ScanResult(endpoints, totalFilesCount, duration, false)
    }

    fun scanKotlinFile(file: KtFile, endpoints: MutableList<ApiNode>) {
        endpoints.addAll(kotlinParser.parseKtFile(file))
    }

    fun scanJavaFile(file: PsiJavaFile, endpoints: MutableList<ApiNode>) {
        endpoints.addAll(javaParser.parseJavaFile(file))
    }
}