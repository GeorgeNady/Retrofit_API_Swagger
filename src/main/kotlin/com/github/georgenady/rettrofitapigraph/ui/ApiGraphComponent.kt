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
import com.mxgraph.util.mxEvent
import com.mxgraph.view.mxGraph
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.SwingConstants

class ApiGraphComponent(private val project: Project) : JBPanel<ApiGraphComponent>(BorderLayout()) {

    var onRefreshRequested: (() -> Unit)? = null

    private val statusLabel = JBLabel("Ready").apply {
        horizontalAlignment = SwingConstants.LEFT
        border = JBUI.Borders.empty(4, 8)
        foreground = JBColor.GRAY
    }

    private val graph = object : mxGraph() {
        override fun convertValueToString(cell: Any?): String {
            val value = model.getValue(cell)
            if (value is ApiNode) {
                return "${value.httpMethod} ${value.path}"
            }
            if (value is String && value == "REFRESH_BUTTON") {
                return "SCAN AGAIN"
            }
            return super.convertValueToString(cell)
        }
    }

    private val graphComponent = mxGraphComponent(graph)

    init {
        graph.isCellsEditable = false
        graphComponent.isConnectable = true
        graphComponent.isDragEnabled = true
        graphComponent.viewport.isOpaque = false
        graphComponent.isOpaque = false
        
        graph.addListener(mxEvent.CELL_CONNECTED) { _, evt ->
            val edge = evt.getProperty("edge")
            val source = graph.model.getTerminal(edge, true)
            val target = graph.model.getTerminal(edge, false)
            
            if (source != null && target != null) {
                val sourceNode = graph.model.getValue(source) as? ApiNode
                val targetNode = graph.model.getValue(target) as? ApiNode
                
                if (sourceNode != null && targetNode != null) {
                    val service = project.service<CacheScaffoldingService>()
                    service.setupCacheDependency(sourceNode, targetNode)
                    setStatus("Dependency Injected: ${sourceNode.methodName} -> ${targetNode.methodName}")
                }
            }
        }

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
                val msg = "NO RETROFIT APIs FOUND"
                graph.insertVertex(parent, null, msg, 40.0, 40.0, 250.0, 30.0, 
                    "fillColor=none;strokeColor=none;fontStyle=1;fontSize=15;align=center;fontColor=#888888")
                
                graph.insertVertex(parent, null, "REFRESH_BUTTON", 85.0, 90.0, 150.0, 45.0,
                    "fillColor=#F57C00;fontColor=#ffffff;strokeColor=#E65100;rounded=1;fontSize=14;fontStyle=1")
                
                return
            }

            // Theming based on IDE
            val textColor = if (JBColor.isBright()) "#333333" else "#A9B7C6"
            val classBg = if (JBColor.isBright()) "#F5F5F5" else "#3C3F41"
            val apiBg = if (JBColor.isBright()) "#E3F2FD" else "#2D394C"

            endpoints.groupBy { it.className }.forEach { (className, methods) ->
                val classVertex = graph.insertVertex(parent, null, className, 0.0, 0.0, 160.0, 40.0, 
                    "fillColor=$classBg;strokeColor=#888888;fontStyle=1;fontColor=$textColor")
                
                methods.forEach { node ->
                    val style = if (node.supportsCache) "fillColor=#2E7D32;fontColor=#ffffff;strokeColor=#1B5E20" 
                                else "fillColor=$apiBg;strokeColor=#2196F3;fontColor=$textColor"
                    val methodVertex = graph.insertVertex(parent, null, node, 0.0, 0.0, 200.0, 40.0, style)
                    graph.insertEdge(parent, null, "", classVertex, methodVertex, "strokeColor=#888888;endArrow=none")
                }
            }
            
            mxHierarchicalLayout(graph).execute(parent)
            
        } finally {
            graph.model.endUpdate()
        }
        graphComponent.refresh()
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
