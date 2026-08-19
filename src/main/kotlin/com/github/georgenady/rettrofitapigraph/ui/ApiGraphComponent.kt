package com.github.georgenady.rettrofitapigraph.ui

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.services.CacheScaffoldingService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.util.mxConstants
import com.mxgraph.util.mxEvent
import com.mxgraph.view.mxGraph
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.SwingConstants

class ApiGraphComponent(private val project: Project) : JBPanel<ApiGraphComponent>(BorderLayout()) {

    var onRefreshRequested: (() -> Unit)? = null

    private val statusLabel = JBLabel("Ready").apply {
        horizontalAlignment = SwingConstants.LEFT
        border = JBUI.Borders.empty(6, 10)
        foreground = JBColor.namedColor("Label.infoForeground", JBColor.GRAY)
    }

    private val graph = object : mxGraph() {
        override fun convertValueToString(cell: Any?): String {
            val value = model.getValue(cell)
            if (value is ApiNode) {
                val pathStr = if (value.path.isNotEmpty()) value.path else "/"
                return "${value.httpMethod}  $pathStr\n(${value.methodName})"
            }
            if (value is String && value == "REFRESH_BUTTON") {
                return "SCAN PROJECT"
            }
            return super.convertValueToString(cell)
        }
    }

    private val graphComponent = mxGraphComponent(graph)

    init {
        graph.isCellsEditable = false
        graph.isCellsResizable = false
        graph.isAllowDanglingEdges = false
        graph.isMultigraph = false

        graphComponent.isConnectable = false
        graphComponent.isDragEnabled = true
        graphComponent.viewport.isOpaque = true
        graphComponent.isOpaque = true
        graphComponent.viewport.background = JBColor.PanelBackground
        graphComponent.background = JBColor.PanelBackground

        add(JBScrollPane(graphComponent).apply {
            border = BorderFactory.createEmptyBorder()
        }, BorderLayout.CENTER)

        add(statusLabel, BorderLayout.SOUTH)
        setupClickListeners()
    }

    fun setStatus(text: String) {
        statusLabel.text = text
    }

    fun updateData(endpoints: List<ApiNode>) {
        val parent = graph.defaultParent
        graph.model.beginUpdate()
        try {
            graph.removeCells(graph.getChildVertices(parent))

            if (endpoints.isEmpty()) {
                val msg = "No Retrofit Endpoints Found"
                graph.insertVertex(
                    parent, null, msg, 40.0, 40.0, 300.0, 35.0,
                    "fillColor=none;strokeColor=none;fontStyle=1;fontSize=16;align=center;fontColor=#888888"
                )

                val hint = "Ensure your interfaces use @GET, @POST, etc. annotations."
                graph.insertVertex(
                    parent, null, hint, 40.0, 80.0, 360.0, 25.0,
                    "fillColor=none;strokeColor=none;fontSize=12;align=center;fontColor=#999999"
                )

                graph.insertVertex(
                    parent, null, "REFRESH_BUTTON", 120.0, 125.0, 160.0, 40.0,
                    "fillColor=#2196F3;fontColor=#ffffff;strokeColor=#1976D2;rounded=1;fontSize=13;fontStyle=1;arcSize=15"
                )
                return
            }

            val isDark = !JBColor.isBright()
            val textColor = if (isDark) "#DFE1E5" else "#1E1F22"
            val classBg = if (isDark) "#2B2D30" else "#E8E9EC"
            val classStroke = if (isDark) "#4E5157" else "#B4B8BF"

            endpoints.groupBy { it.className }.forEach { (className, methods) ->
                val classVertex = graph.insertVertex(
                    parent, null, className, 0.0, 0.0, 220.0, 45.0,
                    "fillColor=$classBg;strokeColor=$classStroke;fontStyle=1;fontSize=13;fontColor=$textColor;rounded=1;arcSize=10"
                )

                methods.forEach { node ->
                    val (methodBg, methodBorder) = getMethodColors(node.httpMethod, isDark)
                    val style = if (node.supportsCache) {
                        "fillColor=#2E7D32;fontColor=#FFFFFF;strokeColor=#4CAF50;rounded=1;fontSize=12;arcSize=10"
                    } else {
                        "fillColor=$methodBg;strokeColor=$methodBorder;fontColor=$textColor;rounded=1;fontSize=12;arcSize=10"
                    }

                    val methodVertex = graph.insertVertex(parent, null, node, 0.0, 0.0, 260.0, 45.0, style)
                    graph.insertEdge(parent, null, "", classVertex, methodVertex, "strokeColor=$classStroke;endArrow=none;verticalAlign=middle")
                }
            }

            val layout = mxHierarchicalLayout(graph)
            layout.orientation = SwingConstants.WEST
            layout.intraCellSpacing = 20.0
            layout.interRankCellSpacing = 50.0
            layout.execute(parent)

        } finally {
            graph.model.endUpdate()
        }
        graphComponent.refresh()
    }

    private fun getMethodColors(httpMethod: String, isDark: Boolean): Pair<String, String> {
        return when (httpMethod.uppercase()) {
            "GET" -> if (isDark) Pair("#1B3B2B", "#2E7D32") else Pair("#E8F5E9", "#4CAF50")
            "POST" -> if (isDark) Pair("#3E2723", "#F57C00") else Pair("#FFF3E0", "#FF9800")
            "PUT" -> if (isDark) Pair("#37474F", "#0288D1") else Pair("#E1F5FE", "#03A9F4")
            "DELETE" -> if (isDark) Pair("#3E1B1B", "#C62828") else Pair("#FFEBEE", "#E53935")
            "PATCH" -> if (isDark) Pair("#2A1B3D", "#7B1FA2") else Pair("#F3E5F5", "#8E24AA")
            else -> if (isDark) Pair("#313438", "#5C6166") else Pair("#F0F1F2", "#A0A4A8")
        }
    }

    private fun setupClickListeners() {
        graphComponent.graphControl.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val cell = graphComponent.getCellAt(e.x, e.y)
                if (cell != null) {
                    val value = graph.model.getValue(cell)
                    if (value is ApiNode) {
                        value.psiElement?.let { element ->
                            if (element is com.intellij.pom.Navigatable && element.canNavigate()) {
                                element.navigate(true)
                            }
                        }
                    } else if (value is String && value == "REFRESH_BUTTON") {
                        onRefreshRequested?.invoke()
                    }
                }
            }

            override fun mouseMoved(e: MouseEvent) {
                val cell = graphComponent.getCellAt(e.x, e.y)
                val value = if (cell != null) graph.model.getValue(cell) else null
                graphComponent.cursor = if (value is ApiNode || (value is String && value == "REFRESH_BUTTON")) {
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                } else {
                    Cursor.getDefaultCursor()
                }
            }
        })
    }
}
