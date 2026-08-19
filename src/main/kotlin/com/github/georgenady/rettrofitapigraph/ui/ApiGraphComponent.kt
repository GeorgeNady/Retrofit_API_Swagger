package com.github.georgenady.rettrofitapigraph.ui

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.SwingConstants

class ApiGraphComponent : JBPanel<ApiGraphComponent>(BorderLayout()) {

    var onRefreshRequested: (() -> Unit)? = null
    private val statusLabel = JBLabel("Ready").apply {
        horizontalAlignment = SwingConstants.LEFT
        border = JBUI.Borders.empty(2, 5)
    }

    private val graph = object : mxGraph() {
        override fun convertValueToString(cell: Any?): String {
            val value = model.getValue(cell)
            if (value is ApiNode) {
                return "${value.httpMethod} ${value.path}"
            }
            if (value is String && value.startsWith("REFRESH_BUTTON")) {
                return "SCAN AGAIN"
            }
            return super.convertValueToString(cell)
        }
    }
    private val graphComponent = mxGraphComponent(graph)

    init {
        graphComponent.isConnectable = false
        graphComponent.isDragEnabled = false
        graphComponent.viewport.isOpaque = false
        graphComponent.isOpaque = false
        
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
                val msg = "NO ENDPOINTS DETECTED\n\n" +
                          "Checklist:\n" +
                          "1. Are your Retrofit interfaces marked with @GET/@POST?\n" +
                          "2. Is the project fully synced (Gradle)?\n" +
                          "3. Are the files within module source roots?"
                
                graph.insertVertex(parent, null, msg, 20.0, 20.0, 350.0, 120.0, 
                    "fillColor=none;strokeColor=none;fontStyle=1;fontSize=13;align=left;verticalAlign=top;fontColor=#888888")
                
                // Big Orange Button
                graph.insertVertex(parent, null, "REFRESH_BUTTON", 20.0, 150.0, 140.0, 45.0,
                    "fillColor=#F57C00;fontColor=#ffffff;strokeColor=#E65100;rounded=1;fontSize=14;fontStyle=1")
                
                return
            }

            val classNodes = mutableMapOf<String, Any>()
            endpoints.groupBy { it.className }.forEach { (className, methods) ->
                val classVertex = graph.insertVertex(parent, null, className, 0.0, 0.0, 160.0, 40.0, 
                    "fillColor=#EEEEEE;strokeColor=#BDBDBD;fontStyle=1;fontColor=#333333")
                classNodes[className] = classVertex
                
                methods.forEach { node ->
                    val methodVertex = graph.insertVertex(parent, null, node, 0.0, 0.0, 200.0, 40.0, 
                        "fillColor=#E3F2FD;strokeColor=#2196F3;fontColor=#0D47A1")
                    graph.insertEdge(parent, null, "", classVertex, methodVertex, "strokeColor=#90CAF9;endArrow=none")
                }
            }
            
            val layout = mxHierarchicalLayout(graph)
            layout.execute(parent)
            
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
                    } else if (value is String && value.startsWith("REFRESH_BUTTON")) {
                        onRefreshRequested?.invoke()
                    }
                }
            }
            
            override fun mouseMoved(e: MouseEvent) {
                val cell = graphComponent.getCellAt(e.x, e.y)
                val value = if (cell != null) graph.model.getValue(cell) else null
                graphComponent.cursor = if (value is ApiNode || (value is String && value.startsWith("REFRESH_BUTTON"))) {
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                } else {
                    Cursor.getDefaultCursor()
                }
            }
        })
    }
}
