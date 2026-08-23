package com.github.georgenady.rettrofitapigraph.presentation.panels.graphPanel

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.main.MainToolViewModel
import com.github.georgenady.rettrofitapigraph.presentation.theme.SwaggerTheme
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.ui.JBColor
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.util.mxConstants
import com.mxgraph.util.mxUtils
import com.mxgraph.view.mxGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLayeredPane
import javax.swing.JPanel
import javax.swing.SwingConstants

class GraphPanel(
    private val project: Project
) : JPanel(BorderLayout()) {

    private val viewModel = project.service<MainToolViewModel>()
    private var subscriptionJob: Job? = null
    private var lastRenderedEndpoints: List<ApiNode>? = null
    private val vertexCache = mutableMapOf<ApiNode, Any>()

    private val graph = object : mxGraph() {
        override fun isCellSelectable(cell: Any?): Boolean {
            val value = model.getValue(cell)
            return value is ApiNode || super.isCellSelectable(cell)
        }

        override fun convertValueToString(cell: Any?): String {
            val value = model.getValue(cell)
            if (value is ApiNode) {
                val method = value.httpMethod.uppercase()
                val theme = SwaggerTheme.getThemeForMethod(method)
                val badgeColor = mxUtils.getHexColorString(theme.badgeColor)
                val textColor = if (JBColor.isBright()) "#1E1F22" else "#DFE1E5"
                val dividerColor = if (JBColor.isBright()) "#E0E0E0" else "#4E5157"
                
                return """
                    <html>
                    <div style="padding: 8px; width: 160px; font-family: sans-serif;">
                        <div style="font-weight: bold; font-size: 10pt; color: $textColor; word-wrap: break-word;">
                            ${value.methodName}
                        </div>
                        <div style="height: 1px; background-color: $dividerColor; margin: 6px 0;"></div>
                        <table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
                            <tr>
                                <td style="width: 1%;">
                                    <div style="background-color: $badgeColor; color: white; padding: 2px 5px; border-radius: 3px; font-weight: bold; font-size: 7.5pt; text-align: center;">
                                        $method
                                    </div>
                                </td>
                                <td style="padding-left: 6px; font-size: 8pt; color: #888888; word-wrap: break-word;">
                                    ${value.path}
                                </td>
                            </tr>
                        </table>
                    </div>
                    </html>
                """.trimIndent()
            }
            if (value is String && value == "REFRESH_BUTTON") {
                return MyBundle.message("empty.scan_again")
            }
            return super.convertValueToString(cell)
        }
    }

    private val graphComponent = mxGraphComponent(graph)
    private val layeredPane = JLayeredPane()
    
    private val overlayToolbar = GraphOverlayToolbar(
        onHandToggled = { setPanMode(it) },
        onZoomIn = { graphComponent.zoomIn() },
        onZoomOut = { graphComponent.zoomOut() },
        onZoomReset = { graphComponent.zoomActual() },
        onZoomFit = { fitGraph() }
    )

    init {
        graph.setHtmlLabels(true)
        setupGraphDefaults()
        
        layeredPane.add(graphComponent, JLayeredPane.DEFAULT_LAYER)
        layeredPane.add(overlayToolbar, JLayeredPane.PALETTE_LAYER)
        
        add(layeredPane, BorderLayout.CENTER)
        
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                graphComponent.bounds = Rectangle(0, 0, width, height)
                positionToolbar()
            }
        })
        
        setupInteractivity()
    }

    override fun addNotify() {
        super.addNotify()
        subscriptionJob = viewModel.viewModelScope.launch(Dispatchers.Main) {
            viewModel.uiState.collectLatest { state ->
                syncSelection(state.selectedNode)
            }
        }
    }

    override fun removeNotify() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        super.removeNotify()
    }

    private fun syncSelection(selectedNode: ApiNode?) {
        val cell = vertexCache[selectedNode]
        graph.setSelectionCell(cell)
    }

    private fun setupGraphDefaults() {
        graph.isCellsEditable = false
        graph.isCellsResizable = false
        graph.isAllowDanglingEdges = false
        graph.isMultigraph = false
        graph.isCellsDisconnectable = false
        graph.isAutoSizeCells = true

        graphComponent.isConnectable = false
        graphComponent.isDragEnabled = true
        graphComponent.isPageVisible = false
        graphComponent.setToolTips(true)
        graphComponent.viewport.isOpaque = true
        graphComponent.isOpaque = true
        
        updateTheme()
    }

    private fun positionToolbar() {
        val toolbarSize = overlayToolbar.preferredSize
        val margin = 20
        overlayToolbar.setBounds(
            width - toolbarSize.width - margin,
            height - toolbarSize.height - margin,
            toolbarSize.width,
            toolbarSize.height
        )
    }

    private fun fitGraph() {
        val bounds = graph.graphBounds
        val view = graphComponent.viewport.bounds
        if (bounds.width > 0 && bounds.height > 0) {
            val scale =
                (view.width.toDouble() / bounds.width).coerceAtMost(view.height.toDouble() / bounds.height)
            graphComponent.zoom(1.0.coerceAtMost(scale))
        }
    }

    private fun setPanMode(enabled: Boolean) {
        graphComponent.panningHandler.isEnabled = enabled
        // Direct field access for mxPanningHandler.useLeftButtonForPanning
        try {
            val field = graphComponent.panningHandler.javaClass.getDeclaredField("useLeftButtonForPanning")
            field.isAccessible = true
            field.setBoolean(graphComponent.panningHandler, enabled)
        } catch (_: Exception) {
            // Fallback or ignore
        }
        
        graphComponent.graphControl.cursor = if (enabled) {
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        } else {
            Cursor.getDefaultCursor()
        }
    }

    fun zoomIn() = graphComponent.zoomIn()
    fun zoomOut() = graphComponent.zoomOut()
    fun zoomActual() = graphComponent.zoomActual()

    private fun updateTheme() {
        val isDark = !JBColor.isBright()
        graphComponent.viewport.background = JBColor.PanelBackground
        graphComponent.background = JBColor.PanelBackground

        val stylesheet = graph.stylesheet
        val defaultVertexStyle = stylesheet.defaultVertexStyle
        val textColor = if (isDark) "#DFE1E5" else "#1E1F22"
        
        defaultVertexStyle[mxConstants.STYLE_FONTCOLOR] = textColor
        defaultVertexStyle[mxConstants.STYLE_STROKECOLOR] = if (isDark) "#4E5157" else "#B4B8BF"
        defaultVertexStyle[mxConstants.STYLE_ROUNDED] = true
        defaultVertexStyle[mxConstants.STYLE_FONTSIZE] = 12
        
        val defaultEdgeStyle = stylesheet.defaultEdgeStyle
        defaultEdgeStyle[mxConstants.STYLE_STROKECOLOR] = if (isDark) "#555555" else "#CCCCCC"
        defaultEdgeStyle[mxConstants.STYLE_ENDARROW] = mxConstants.ARROW_CLASSIC
        defaultEdgeStyle[mxConstants.STYLE_EDGE] = mxConstants.EDGESTYLE_ORTHOGONAL
    }

    private fun setupInteractivity() {
        graphComponent.addMouseWheelListener { e ->
            if (e.isControlDown) {
                if (e.wheelRotation < 0) graphComponent.zoomIn() else graphComponent.zoomOut()
            }
        }

        graphComponent.graphControl.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val cell = graphComponent.getCellAt(e.x, e.y)
                val value = if (cell != null) graph.model.getValue(cell) else null
                if (value is ApiNode) {
                    viewModel.selectNode(value)
                    if (e.clickCount == 2) {
                        value.psiElement?.let { element ->
                            val navigable = element as? Navigatable
                            val canNavigate = ReadAction.compute<Boolean, Throwable> {
                                navigable?.canNavigate() == true
                            }
                            if (canNavigate) {
                                navigable?.navigate(true)
                            }
                        }
                    }
                } else {
                    viewModel.selectNode(null)
                }
            }
        })
    }

    fun render(endpoints: List<ApiNode>) {
        if (lastRenderedEndpoints == endpoints) {
            return
        }
        lastRenderedEndpoints = endpoints
        
        val parent = graph.defaultParent
        graph.model.beginUpdate()
        try {
            graph.removeCells(graph.getChildVertices(parent))
            vertexCache.clear()

            if (endpoints.isEmpty()) {
                val msg = MyBundle.message("empty.nothing_found")
                graph.insertVertex(parent, null, msg, 40.0, 40.0, 250.0, 30.0, 
                    "fillColor=none;strokeColor=none;fontStyle=1;fontSize=15;align=center;fontColor=#888888")
                
                graph.insertVertex(parent, null, "REFRESH_BUTTON", 85.0, 90.0, 150.0, 45.0,
                    "fillColor=#F57C00;fontColor=#ffffff;strokeColor=#E65100;rounded=1;fontSize=14;fontStyle=1")
                
                return
            }

            val classNodes = mutableMapOf<String, Any>()
            val isDark = !JBColor.isBright()
            val textColor = if (isDark) "#DFE1E5" else "#1E1F22"
            val classBg = if (isDark) "#2B2D30" else "#E8E9EC"

            endpoints.groupBy { it.className }.forEach { (className, methods) ->
                val classVertex = graph.insertVertex(
                    parent, null, className, 0.0, 0.0, 180.0, 40.0,
                    "fillColor=$classBg;fontStyle=1;fontSize=13;fontColor=$textColor;arcSize=10"
                )
                classNodes[className] = classVertex

                methods.forEach { node ->
                    val theme = SwaggerTheme.getThemeForMethod(node.httpMethod)
                    val bgColor = mxUtils.getHexColorString(theme.backgroundColor)
                    val borderColor = mxUtils.getHexColorString(theme.borderColor)
                    
                    val style = "fillColor=$bgColor;strokeColor=$borderColor;fontColor=$textColor;arcSize=10"
                    val methodVertex = graph.insertVertex(parent, null, node, 0.0, 0.0, 180.0, 55.0, style)
                    vertexCache[node] = methodVertex
                    graph.updateCellSize(methodVertex)
                    graph.insertEdge(parent, null, "", classVertex, methodVertex)
                }
            }

            val layout = mxHierarchicalLayout(graph)
            layout.orientation = SwingConstants.WEST
            layout.intraCellSpacing = 30.0
            layout.interRankCellSpacing = 100.0
            layout.execute(parent)

        } finally {
            graph.model.endUpdate()
        }
        graphComponent.refresh()
    }
}
