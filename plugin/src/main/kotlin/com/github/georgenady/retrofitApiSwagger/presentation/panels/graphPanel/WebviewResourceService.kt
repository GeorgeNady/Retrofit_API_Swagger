package com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel

import com.intellij.openapi.components.Service
import com.intellij.openapi.util.io.FileUtil
import java.io.File

@Service(Service.Level.PROJECT)
class WebviewResourceService {

    /**
     * Extracts the single-file React build from resources to a temporary directory.
     * This avoids CORS issues with file:// protocol.
     */
    fun extractResources(): File {
        val tempDir = FileUtil.createTempDirectory("retrofit-webview", null)
        
        // Single-file build only produces index.html
        val indexFile = File(tempDir, "index.html")
        extractResource("/webview/index.html", indexFile)

        return tempDir
    }

    private fun extractResource(resourcePath: String, outputFile: File): Boolean {
        return javaClass.getResourceAsStream(resourcePath)?.use { input ->
            outputFile.parentFile.mkdirs()
            FileUtil.writeToFile(outputFile, input.readBytes())
            true
        } ?: false
    }
}
