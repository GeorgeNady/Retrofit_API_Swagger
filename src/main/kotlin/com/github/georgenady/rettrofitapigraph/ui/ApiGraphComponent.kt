package com.github.georgenady.rettrofitapigraph.ui

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.services.CacheScaffoldingService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
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
    private val statusLabel = JBLabel("Design Mode: Draw arrows to set cache invalidation").apply {
        horizontalAlignment = SwingConstants.LEFT
        border = JBUI.Borders.empty(2, 5)
    }

    private val graph = object : mxGraph() {
        override fun convertValueToString(cell: Any?): String {
            val value = model.getValue(cell)
            if (value is ApiNode) {
                return "${value.httpMethod} ${value.path}"
            }
            return super.convertValueToString(cell)
        }
    }
    
    private val graphComponent = mxGraphComponent(graph)

    init {
        graph.isCellsEditable = false
        graph.isAllowDanglingEdges = false
        graphComponent.isConnectable = true
        graphComponent.isDragEnabled = true
        
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
                    statusLabel.text = "Injected cache dependency: ${sourceNode.methodName} -> ${targetNode.methodName}"
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
                val msg = "NO ENDPOINTS DETECTED\n\n" +
                          "Checklist:\n" +
                          "1. Are your Retrofit interfaces marked with @GET/@POST?\n" +
                          "2. Is the project fully synced (Gradle)?\n" +
                          "3. Are the files within module source roots?"
                
                graph.insertVertex(parent, null, msg, 20.0, 20.0, 350.0, 120.0, 
                    "fillColor=none;strokeColor=none;fontStyle=1;fontSize=13;align=left;verticalAlign=top;fontColor=#888888")
                
                graph.insertVertex(parent, null, "REFRESH_BUTTON", 20.0, 150.0, 140.0, 45.0,
                    "fillColor=#F57C00;fontColor=#ffffff;strokeColor=#E65100;rounded=1;fontSize=14;fontStyle=1")
                
                return
            }

            endpoints.forEach { node ->
                val style = if (node.supportsCache) "fillColor=#C8E6C9;strokeColor=#4CAF50;fontColor=#000000" 
                            else "fillColor=#E3F2FD;strokeColor=#2196F3;fontColor=#000000"
                graph.insertVertex(parent, null, node, 0.0, 0.0, 200.0, 40.0, style)
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
                    }
                }
            }
        })
    }
}
