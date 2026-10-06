package com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.presentation.main.MainToolViewModel
import com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel.WebviewResourceService
import com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel.utils.toGraphPayload
import com.google.gson.Gson
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Handles JCEF browser creation and operations in an isolated class.
 * This ensures that if JCEF cannot be loaded, the JVM only fails inside this adapter
 * rather than crashing SwaggerPanel and MainToolWindow during class verification.
 */
class JcefWebviewAdapter(
    private val project: Project,
    private val viewModel: MainToolViewModel,
    private val resourceService: WebviewResourceService,
    private val isEditorMode: Boolean,
    private val targetFile: VirtualFile?,
    private val initialViewMode: String
) {
    private val gson = Gson()
    private var isReady = false
    private var pendingEndpoints: List<ApiNode>? = null
    private var jsBridge: SwaggerJavascriptBridge? = null

    val browser: JBCefBrowser = JBCefBrowser.createBuilder()
        .setOffScreenRendering(false)
        .build()

    val component: JComponent
        get() = browser.component

    init {
        setupBrowser()
    }

    private fun setupBrowser() {
        val b = browser
        jsBridge = SwaggerJavascriptBridge(b, viewModel, { targetFile }) {
            isReady = true
            val toRender = pendingEndpoints ?: if (isEditorMode && targetFile != null && targetFile.isValid) {
                project.service<com.github.georgenady.retrofitApiSwagger.domain.repository.ApiRepository>()
                    .findRetrofitEndpointsInFile(targetFile)
            } else null

            toRender?.let { render(it) }
            pendingEndpoints = null
        }

        b.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                val query = jsBridge?.jsQuery?.inject("query.request") ?: return
                browser?.executeJavaScript(
                    "window.cefQuery = function(query) { $query };",
                    browser.url ?: "", 0
                )
            }
            override fun onLoadingStateChange(browser: CefBrowser?, isLoading: Boolean, canGoBack: Boolean, canGoForward: Boolean) {
                if (!isLoading) {
                    val query = jsBridge?.jsQuery?.inject("query.request") ?: return
                    browser?.executeJavaScript(
                        "window.cefQuery = function(query) { $query };",
                        browser.url ?: "", 0
                    )
                }
            }
        }, b.cefBrowser)

        // Native Context Menu for DevTools
        b.jbCefClient.addContextMenuHandler(object : CefContextMenuHandlerAdapter() {
            override fun onBeforeContextMenu(
                browser: CefBrowser?,
                frame: CefFrame?,
                params: CefContextMenuParams?,
                model: CefMenuModel?
            ) {
                model?.clear()
                model?.addItem(CefMenuModel.MenuId.MENU_ID_USER_FIRST, "Refresh Swagger")
                model?.addItem(CefMenuModel.MenuId.MENU_ID_USER_FIRST + 1, "Open DevTools")
            }

            override fun onContextMenuCommand(
                browser: CefBrowser?,
                frame: CefFrame?,
                params: CefContextMenuParams?,
                commandId: Int,
                eventFlags: Int
            ): Boolean {
                when (commandId) {
                    CefMenuModel.MenuId.MENU_ID_USER_FIRST -> {
                        b.loadURL(b.cefBrowser.url)
                        return true
                    }
                    CefMenuModel.MenuId.MENU_ID_USER_FIRST + 1 -> {
                        b.openDevtools()
                        return true
                    }
                }
                return false
            }
        }, b.cefBrowser)

        val extractedDir = resourceService.extractResources()
        val indexFile = File(extractedDir, "index.html")
        if (indexFile.exists()) {
            val url = indexFile.toURI().toURL().toString() + "?view=$initialViewMode&editor=$isEditorMode"
            println("RetrofitSwagger Debug: Loading Swagger index URL: $url")
            b.loadURL(url)
        } else {
            println("RetrofitSwagger Error: index.html missing from extracted resources at ${extractedDir.absolutePath}")
        }

        // F12 to open DevTools
        b.component.addKeyListener(object : java.awt.event.KeyAdapter() {
            override fun keyPressed(e: java.awt.event.KeyEvent) {
                if (e.keyCode == java.awt.event.KeyEvent.VK_F12) {
                    b.openDevtools()
                }
            }
        })
    }

    fun render(endpoints: List<ApiNode>) {
        if (!isReady) {
            pendingEndpoints = endpoints
            return
        }

        val edgeActions = com.github.georgenady.retrofitApiSwagger.data.service.EdgeActionSettingsService.getInstance(viewModel.project).state.actions
        val statePayload = endpoints.toGraphPayload(
            requestResults = viewModel.uiState.value.requestResults,
            isEditorMode = isEditorMode,
            edgeActions = edgeActions
        )
        val jsonPayload = gson.toJson(statePayload)

        val jsCode = "window.updateGraphData(${gson.toJson(jsonPayload)}, ${statePayload["isDark"]})"
        browser.cefBrowser.executeJavaScript(jsCode, browser.cefBrowser.url, 0)
    }

    fun setViewMode(mode: String) {
        val js = "if (window.setViewMode) window.setViewMode('$mode');"
        browser.cefBrowser.executeJavaScript(js, browser.cefBrowser.url ?: "", 0)
    }

    fun updateResponseResults(results: Map<String, Any?>) {
        if (isReady) {
            results.forEach { (sig, res) ->
                val js = "if (window.updateResponseResult) window.updateResponseResult(${gson.toJson(sig)}, ${gson.toJson(res)});"
                browser.cefBrowser.executeJavaScript(js, browser.cefBrowser.url ?: "", 0)
            }
        }
    }

    fun isRealBrowserInitialized(): Boolean {
        return try {
            val comp = browser.component
            if (containsJcefErrorMessage(comp)) return false
            browser.cefBrowser
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun containsJcefErrorMessage(comp: java.awt.Component): Boolean {
        if (comp is javax.swing.JLabel) {
            val text = comp.text ?: ""
            if (text.contains("not available in this IDE runtime", ignoreCase = true) ||
                text.contains("JetBrains Runtime (JBR)", ignoreCase = true) ||
                text.contains("JCEF browser is not available", ignoreCase = true) ||
                text.contains("JCEF is not supported", ignoreCase = true)
            ) {
                return true
            }
        }
        if (comp is java.awt.Container) {
            for (child in comp.components) {
                if (containsJcefErrorMessage(child)) return true
            }
        }
        return false
    }

    companion object {
        fun isJcefAvailable(): Boolean {
            return try {
                if (!JBCefApp.isSupported()) return false
                JBCefApp.getInstance()
                true
            } catch (_: Throwable) {
                false
            }
        }
    }
}
