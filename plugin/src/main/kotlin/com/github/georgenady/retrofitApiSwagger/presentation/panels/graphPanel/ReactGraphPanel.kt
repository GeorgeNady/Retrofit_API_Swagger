package com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.presentation.main.MainToolViewModel
import com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel.utils.toGraphPayload
import com.google.gson.Gson
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
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
import javax.swing.JLabel
import javax.swing.JPanel

class ReactGraphPanel(project: Project) : JPanel(BorderLayout()) {

    private val viewModel = project.service<MainToolViewModel>()
    private val resourceService = project.service<WebviewResourceService>()

    private val browser = if (JBCefApp.isSupported()) {
        JBCefBrowser.createBuilder()
            .setOffScreenRendering(false)
            .build()
    } else null
    private val gson = Gson()

    private var isReady = false
    private var pendingEndpoints: List<ApiNode>? = null
    private var jsBridge: GraphJavascriptBridge? = null

    init {
        if (browser == null) {
            add(JLabel("JCEF is not supported. Please use a different IDE runtime."), BorderLayout.CENTER)
        } else {
            setupBrowser(browser)
        }
    }

    private fun setupBrowser(b: JBCefBrowser) {
        add(b.component, BorderLayout.CENTER)

        jsBridge = GraphJavascriptBridge(b, viewModel) {
            isReady = true
            pendingEndpoints?.let { render(it) }
            pendingEndpoints = null
        }

        b.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript(
                    "window.cefQuery = function(query) { ${jsBridge?.jsQuery?.inject("query.request")} };",
                    browser.url ?: "", 0
                )
            }
        }, b.cefBrowser)

        // Native Context Menu for reliable DevTools access
        b.jbCefClient.addContextMenuHandler(object : CefContextMenuHandlerAdapter() {
            override fun onBeforeContextMenu(browser: CefBrowser?, frame: CefFrame?, params: CefContextMenuParams?, model: CefMenuModel?) {
                model?.clear()
                model?.addItem(CefMenuModel.MenuId.MENU_ID_USER_FIRST, "Refresh Graph")
                model?.addItem(CefMenuModel.MenuId.MENU_ID_USER_FIRST + 1, "Open DevTools")
            }

            override fun onContextMenuCommand(browser: CefBrowser?, frame: CefFrame?, params: CefContextMenuParams?, commandId: Int, eventFlags: Int): Boolean {
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
            val url = indexFile.toURI().toURL().toString() + "?view=graph"
            println("RetrofitSwagger Debug: Loading index URL: $url")
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
        val b = browser ?: return
        if (!isReady) {
            pendingEndpoints = endpoints
            return
        }

        val edgeActions = com.github.georgenady.retrofitApiSwagger.data.service.EdgeActionSettingsService.getInstance(viewModel.project).state.actions
        val statePayload = endpoints.toGraphPayload(edgeActions = edgeActions)
        val jsonPayload = gson.toJson(statePayload)

        val jsCode = "window.updateGraphData(${gson.toJson(jsonPayload)}, ${statePayload["isDark"]})"
        b.cefBrowser.executeJavaScript(jsCode, b.cefBrowser.url, 0)
    }

    fun setViewMode(mode: String) {
        val b = browser ?: return
        val js = "if (window.setViewMode) window.setViewMode('$mode');"
        b.cefBrowser.executeJavaScript(js, b.cefBrowser.url ?: "", 0)
    }
}
