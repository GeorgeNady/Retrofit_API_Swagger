package com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel

import com.github.georgenady.retrofitApiSwagger.domain.usecase.ApiParamPayload
import com.github.georgenady.retrofitApiSwagger.domain.usecase.CreateOrUpdateApiRequest
import com.github.georgenady.retrofitApiSwagger.domain.usecase.CreateOrUpdateApiUseCase
import com.github.georgenady.retrofitApiSwagger.domain.usecase.FindPsiClassUseCase
import com.github.georgenady.retrofitApiSwagger.domain.usecase.GenerateJsonSchemaUseCase
import com.github.georgenady.retrofitApiSwagger.presentation.main.MainToolViewModel
import com.google.gson.Gson
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.datatransfer.StringSelection

class SwaggerJavascriptBridge(
    private val browserBase: JBCefBrowserBase,
    private val viewModel: MainToolViewModel,
    private val targetFileProvider: () -> VirtualFile?,
    private val onReady: () -> Unit
) {
    private val gson = Gson()
    val jsQuery: JBCefJSQuery = JBCefJSQuery.create(browserBase)

    init {
        jsQuery.addHandler { request ->
            handleRequest(request)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleRequest(request: String) {
        val map = gson.fromJson(request, Map::class.java)
        when (map["type"]) {
            "ready" -> onReady()
            "log" -> println("RetrofitSwagger JS Console: ${map["message"]}")
            "error" -> {
                val message = map["message"] as? String ?: "An unknown error occurred"
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Retrofit API Swagger Notification Group")
                    .createNotification("Swagger Error", message, NotificationType.ERROR)
                    .notify(viewModel.project)
            }
            "nodeSelected" -> {
                val signature = map["signature"] as? String
                viewModel.uiState.value.allEndpoints.find { it.signature == signature }?.let {
                    viewModel.selectNode(it)
                }
            }
            "navigateToSource" -> {
                val signature = map["signature"] as? String
                val node = viewModel.uiState.value.allEndpoints.find { it.signature == signature }
                node?.psiElement?.let { element ->
                    ApplicationManager.getApplication().invokeLater {
                        ReadAction.run<Throwable> {
                            (element as? Navigatable)?.navigate(true)
                        }
                    }
                }
            }
            "executeApiCall" -> {
                val signature = map["signature"] as? String
                val url = map["url"] as? String ?: ""
                val body = map["body"] as? String
                val node = viewModel.uiState.value.allEndpoints.find { it.signature == signature }
                if (node != null) {
                    viewModel.executeApiCall(node, url, body)
                }
            }
            "copyToClipboard" -> {
                val text = map["text"] as? String ?: ""
                CopyPasteManager.getInstance().setContents(StringSelection(text))
            }
            "navigateToType" -> {
                val typeName = map["typeName"] as? String ?: ""
                val interfaceClassName = map["interfaceClassName"] as? String ?: ""
                viewModel.viewModelScope.launch {
                    val findClassUseCase = FindPsiClassUseCase(viewModel.project)
                    val psiClass = findClassUseCase(typeName, interfaceClassName)
                    withContext(Dispatchers.Main) {
                        psiClass?.navigate(true)
                    }
                }
            }
            "requestSchema" -> {
                val fqn = map["fqn"] as? String ?: ""
                if (fqn.isNotBlank()) {
                    viewModel.viewModelScope.launch(Dispatchers.Default) {
                        val generateUseCase = GenerateJsonSchemaUseCase(viewModel.project)
                        val schemaJson = try {
                            generateUseCase(fqn)
                        } catch (e: Exception) {
                            "{}"
                        }
                        withContext(Dispatchers.Main) {
                            val js = "if (window.updateSchema) window.updateSchema(${gson.toJson(fqn)}, ${gson.toJson(schemaJson)});"
                            browserBase.cefBrowser.executeJavaScript(js, browserBase.cefBrowser.url ?: "", 0)
                        }
                    }
                }
            }
            "createOrUpdateApi" -> {
                val targetFile = targetFileProvider()
                if (targetFile == null) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Retrofit API Swagger Notification Group")
                        .createNotification("Create/Update API", "Cannot modify API outside editor mode.", NotificationType.WARNING)
                        .notify(viewModel.project)
                    return
                }

                try {
                    val isUpdate = map["isUpdate"] as? Boolean ?: false
                    val originalSignature = map["originalSignature"] as? String
                    val httpMethod = map["httpMethod"] as? String ?: "GET"
                    val path = map["path"] as? String ?: "/"
                    val methodName = map["methodName"] as? String ?: "newEndpoint"
                    val returnType = map["returnType"] as? String ?: "Unit"
                    val isSuspend = map["isSuspend"] as? Boolean ?: true

                    val rawParams = map["parameters"] as? List<Map<String, Any>> ?: emptyList()
                    val params = rawParams.map { p ->
                        ApiParamPayload(
                            name = p["name"] as? String ?: "param",
                            location = p["location"] as? String ?: "QUERY",
                            type = p["type"] as? String ?: "String"
                        )
                    }

                    val requestObj = CreateOrUpdateApiRequest(
                        isUpdate = isUpdate,
                        originalSignature = originalSignature,
                        httpMethod = httpMethod,
                        path = path,
                        methodName = methodName,
                        returnType = returnType,
                        isSuspend = isSuspend,
                        parameters = params
                    )

                    val useCase = CreateOrUpdateApiUseCase(viewModel.project)
                    val result = useCase(targetFile, requestObj)

                    result.onSuccess {
                        val action = if (isUpdate) "updated" else "created"
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Retrofit API Swagger Notification Group")
                            .createNotification("API $action", "Successfully $action endpoint '$methodName' in ${targetFile.name}", NotificationType.INFORMATION)
                            .notify(viewModel.project)
                    }.onFailure { err ->
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Retrofit API Swagger Notification Group")
                            .createNotification("Error modifying API", err.message ?: "Unknown error", NotificationType.ERROR)
                            .notify(viewModel.project)
                    }
                } catch (e: Exception) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Retrofit API Swagger Notification Group")
                        .createNotification("Error modifying API", e.message ?: "Unknown error", NotificationType.ERROR)
                        .notify(viewModel.project)
                }
            }
            "switchViewMode" -> {
                val mode = map["mode"] as? String ?: "list"
                val targetMode = if (mode.equals("graph", ignoreCase = true)) com.github.georgenady.retrofitApiSwagger.domain.model.enums.ViewMode.GRAPH else com.github.georgenady.retrofitApiSwagger.domain.model.enums.ViewMode.LIST
                viewModel.setViewMode(targetMode)
            }
            "linkNodes" -> {
                val sourceSig = map["source"] as? String
                val targetSig = map["target"] as? String
                val allNodes = viewModel.uiState.value.allEndpoints
                val sourceNode = allNodes.find { it.signature == sourceSig }
                val targetNode = allNodes.find { it.signature == targetSig }
                if (sourceNode != null && targetNode != null) {
                    viewModel.linkApiNodes(sourceNode, targetNode)
                }
            }
        }
    }
}
