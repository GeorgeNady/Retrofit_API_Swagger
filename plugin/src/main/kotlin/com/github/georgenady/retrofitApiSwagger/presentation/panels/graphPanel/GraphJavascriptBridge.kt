package com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel

import com.github.georgenady.retrofitApiSwagger.presentation.main.MainToolViewModel
import com.google.gson.Gson
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery

class GraphJavascriptBridge(
    browserBase: JBCefBrowserBase,
    private val viewModel: MainToolViewModel,
    private val onReady: () -> Unit
) {
    private val gson = Gson()
    val jsQuery = JBCefJSQuery.create(browserBase)

    init {
        jsQuery.addHandler { request ->
            handleRequest(request)
            null
        }
    }

    private fun handleRequest(request: String) {
        val map = gson.fromJson(request, Map::class.java)
        when (map["type"]) {
            "ready" -> onReady()
            "log" -> println("RetrofitSwagger JS Console: ${map["message"]}")
            "error" -> {
                val message = map["message"] as? String ?: "An unknown error occurred"
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Retrofit API Swagger Notification Group")
                    .createNotification("API graph error", message, NotificationType.ERROR)
                    .notify(viewModel.project)
            }
            "nodeSelected" -> {
                val signature = map["signature"] as? String
                viewModel.uiState.value.allEndpoints.find { it.signature == signature }?.let { 
                    viewModel.selectNode(it) 
                }
            }
            "linkNodes" -> {
                val sourceSig = map["source"] as? String
                val targetSig = map["target"] as? String
                val allNodes = viewModel.uiState.value.allEndpoints
                val sourceNode = allNodes.find { it.signature == sourceSig }
                val targetNode = allNodes.find { it.signature == targetSig }
                val defaultActionId = com.github.georgenady.retrofitApiSwagger.data.service.EdgeActionSettingsService.getInstance(viewModel.project).state.actions.firstOrNull()?.id
                if (sourceNode != null && targetNode != null && defaultActionId != null) {
                    try {
                        com.github.georgenady.retrofitApiSwagger.domain.edgeaction.engine.EdgeActionExecutor(viewModel.project)
                            .execute(defaultActionId, sourceNode, targetNode)
                        viewModel.refresh()
                    } catch (_: Exception) {}
                }
            }
            "executeEdgeAction" -> {
                val actionId = map["actionId"] as? String ?: return
                val sourceSig = map["source"] as? String ?: return
                val targetSig = map["target"] as? String ?: return
                val allNodes = viewModel.uiState.value.allEndpoints
                val sourceNode = allNodes.find { it.signature == sourceSig }
                val targetNode = allNodes.find { it.signature == targetSig }
                if (sourceNode != null && targetNode != null) {
                    try {
                        com.github.georgenady.retrofitApiSwagger.domain.edgeaction.engine.EdgeActionExecutor(viewModel.project)
                            .execute(actionId, sourceNode, targetNode)
                        viewModel.refresh()
                    } catch (e: Exception) {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Retrofit API Swagger Notification Group")
                            .createNotification("Edge Action Failed", e.message ?: "Unknown error", NotificationType.ERROR)
                            .notify(viewModel.project)
                    }
                }
            }
            "switchViewMode" -> {
                val mode = map["mode"] as? String ?: "graph"
                val targetMode = if (mode.equals("graph", ignoreCase = true)) com.github.georgenady.retrofitApiSwagger.domain.model.enums.ViewMode.GRAPH else com.github.georgenady.retrofitApiSwagger.domain.model.enums.ViewMode.LIST
                viewModel.setViewMode(targetMode)
            }
        }
    }
}
