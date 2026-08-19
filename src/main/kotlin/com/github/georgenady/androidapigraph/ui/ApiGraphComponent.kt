package com.github.georgenady.androidapigraph.ui

import com.github.georgenady.androidapigraph.model.ApiNode
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory

class ApiGraphComponent : JBPanel<ApiGraphComponent>(BorderLayout()) {

    var onRefreshRequested: (() -> Unit)? = null

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
        
        setupClickListeners()
    }

    fun updateData(endpoints: List<ApiNode>) {
        val parent = graph.defaultParent
        graph.model.beginUpdate()
        try {
            graph.removeCells(graph.getChildVertices(parent))
            
            if (endpoints.isEmpty()) {
                val msg = "NOTHING FOUND\n\nPossible reasons:\n1. Gradle sync incomplete\n2. Indexing not finished\n3. No @GET/@POST annotations"
                graph.insertVertex(parent, null, msg, 20.0, 20.0, 300.0, 100.0, 
                    "fillColor=none;strokeColor=none;fontStyle=1;fontSize=14;align=left;verticalAlign=top")
                
                // Emergency Button in the Graph
                graph.insertVertex(parent, null, "REFRESH_BUTTON", 20.0, 130.0, 120.0, 40.0,
                    "fillColor=#ff9800;fontColor=#ffffff;strokeColor=#e65100;rounded=1;fontSize=14;fontStyle=1")
                
                return
            }

            val classNodes = mutableMapOf<String, Any>()
            endpoints.groupBy { it.className }.forEach { (className, methods) ->
                val classVertex = graph.insertVertex(parent, null, className, 0.0, 0.0, 140.0, 40.0, "fillColor=#f5f5f5;strokeColor=#bdbdbd;fontStyle=1")
                classNodes[className] = classVertex
                
                methods.forEach { node ->
                    val methodVertex = graph.insertVertex(parent, null, node, 0.0, 0.0, 180.0, 40.0, "fillColor=#e3f2fd;strokeColor=#2196f3")
                    graph.insertEdge(parent, null, "", classVertex, methodVertex, "strokeColor=#90caf9")
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
