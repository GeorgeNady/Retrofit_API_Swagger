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
                return "Click here to REFRESH"
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
                val msg = "No Retrofit endpoints found.\n" +
                          "Suggestions:\n" +
                          "- Ensure your project is synced with Gradle.\n" +
                          "- Check if @GET/@POST/etc. are correctly imported.\n" +
                          "- Ensure you are in a module with source code."
                
                graph.insertVertex(parent, null, msg, 20.0, 20.0, 350.0, 80.0, 
                    "fillColor=none;strokeColor=none;fontStyle=2;fontSize=12;align=left;verticalAlign=top")
                
                // Interactive Refresh vertex
                graph.insertVertex(parent, null, "REFRESH_BUTTON", 20.0, 110.0, 150.0, 40.0,
                    "fillColor=#4caf50;fontColor=#ffffff;strokeColor=#2e7d32;rounded=1;fontSize=13;fontStyle=1")
                
                return
            }

            val classNodes = mutableMapOf<String, Any>()
            
            endpoints.groupBy { it.className }.forEach { (className, methods) ->
                val classVertex = graph.insertVertex(parent, null, className, 0.0, 0.0, 120.0, 40.0, "fillColor=#f0f0f0;fontStyle=1")
                classNodes[className] = classVertex
                
                methods.forEach { node ->
                    val methodVertex = graph.insertVertex(parent, null, node, 0.0, 0.0, 150.0, 50.0, "fillColor=#e1f5fe")
                    graph.insertEdge(parent, null, "", classVertex, methodVertex)
                }
            }
            
            // Apply layout
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
