package com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel.utils

import com.github.georgenady.retrofitApiSwagger.domain.edgeaction.model.EdgeActionConfig
import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.presentation.theme.SwaggerTheme
import com.intellij.ui.JBColor
import java.awt.Color

fun List<ApiNode>.toGraphPayload(
    requestResults: Map<String, String> = emptyMap(),
    isEditorMode: Boolean = false,
    edgeActions: List<EdgeActionConfig> = emptyList()
): Map<String, Any> {
    val jsonData = this.map { node ->
        mapOf(
            "methodName" to node.methodName,
            "httpMethod" to node.httpMethod,
            "path" to node.path,
            "className" to node.className,
            "signature" to node.signature,
            "invalidatesKeys" to node.invalidatesKeys,
            "annotations" to node.annotations.map { 
                mapOf("name" to it.name, "arguments" to it.arguments)
            },
            "parameters" to node.parameters.map { param ->
                mapOf(
                    "name" to param.name,
                    "type" to param.type,
                    "location" to param.location.name,
                    "fqn" to (param.fqn ?: "")
                )
            }
        )
    }

    val methodColors = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS").associateWith { 
        val theme = SwaggerTheme.getThemeForMethod(it)
        mapOf(
            "bg" to theme.backgroundColor.toHex(),
            "border" to theme.borderColor.toHex(),
            "badge" to theme.badgeColor.toHex()
        )
    }

    val actionsData = edgeActions.map { action ->
        mapOf(
            "id" to action.id,
            "name" to action.name,
            "description" to action.description,
            "icon" to action.icon,
            "placement" to action.placement.name
        )
    }

    return mapOf(
        "endpoints" to jsonData,
        "colors" to methodColors,
        "isDark" to !JBColor.isBright(),
        "requestResults" to requestResults,
        "isEditorMode" to isEditorMode,
        "edgeActions" to actionsData
    )
}

private fun Color.toHex(): String = String.format("#%02x%02x%02x", red, green, blue)
