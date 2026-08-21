package com.github.georgenady.rettrofitapigraph.presentation.graph

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.presentation.theme.SwaggerTheme
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.mxgraph.layout.mxFastOrganicLayout
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import javax.swing.JButton
import javax.swing.JPanel

class FreeCanvasGraphPanel(
    private val project: Project,
    private val onNodeSelected: ((ApiNode) -> Unit)? = null
) : JBPanel<FreeCanvasGraphPanel>(BorderLayout()) {

    private val graph = object : mxGraph() {
        override fun convertValueToString(cell: Any?): String {
            val value = model.getValue(cell)
            if (value is ApiNode) {
                val pathStr = value.path.ifEmpty { "/" }
                return "${value.httpMethod}  $pathStr\n(${value.methodName})"
            }
            return super.convertValueToString(cell)
        }
    }

    private val graphComponent = mxGraphComponent(graph).apply {
        isConnectable = false
        viewport.isOpaque = true
        isOpaque = true
        viewport.background = JBColor.PanelBackground
        background = JBColor.PanelBackground

        isPanning = true
        graph.isCellsMovable = true
        graph.isCellsResizable = false
        graph.isCellsEditable = false
        graph.isDropEnabled = false
    }

    init {
        val toolbar = createZoomToolbar()

        add(toolbar, BorderLayout.NORTH)
        add(graphComponent, BorderLayout.CENTER)

        setupInteractions()
    }

    private fun createZoomToolbar(): JPanel {
        return JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            isOpaque = false
            border = JBUI.Borders.empty(4, 8)

            add(JButton("Zoom In", AllIcons.General.ZoomIn).apply {
                addActionListener { graphComponent.zoomIn() }
            })

            add(JButton("Zoom Out", AllIcons.General.ZoomOut).apply {
                addActionListener { graphComponent.zoomOut() }
            })

            add(JButton("100%", AllIcons.General.ActualZoom).apply {
                addActionListener { graphComponent.zoomActual() }
            })

            add(JButton("Fit to Screen", AllIcons.General.FitContent).apply {
                addActionListener { zoomToFit() }
            })
        }
    }

    // Helper to convert Java AWT Color to jGraphX Hex format
    private fun Color.toHex(): String = String.format("#%02X%02X%02X", red, green, blue)

    fun render(endpoints: List<ApiNode>) {
        val parent = graph.defaultParent
        graph.model.beginUpdate()
        try {
            graph.removeCells(graph.getChildVertices(parent))

            val isDark = !JBColor.isBright()
            val textColorHex = if (isDark) "#DFE1E5" else "#1E1F22"

            endpoints.forEach { node ->
                // Fetch the HTTP theme from your custom SwaggerTheme
                val theme = SwaggerTheme.getThemeForMethod(node.httpMethod)

                // Convert JBColor to Hex String for jGraphX
                val bgHex = theme.backgroundColor.toHex()
                val borderHex = theme.borderColor.toHex()

                val style = "fillColor=$bgHex;strokeColor=$borderHex;fontColor=$textColorHex;rounded=1;fontSize=12;arcSize=10"
                graph.insertVertex(parent, null, node, 0.0, 0.0, 240.0, 45.0, style)
            }

            val layout = mxFastOrganicLayout(graph).apply {
                forceConstant = 120.0
            }
            layout.execute(parent)

        } finally {
            graph.model.endUpdate()
        }

        graphComponent.zoomActual()
        zoomToFit()
        graphComponent.refresh()
    }

    private fun zoomToFit() {
        val bounds = graph.graphBounds
        val viewPortSize = graphComponent.viewport.size

        if (bounds != null && bounds.width > 0 && bounds.height > 0) {
            val scaleX = viewPortSize.width / bounds.width
            val scaleY = viewPortSize.height / bounds.height
            val targetScale = minOf(scaleX, scaleY) * 0.9

            graph.view.scale = targetScale.coerceIn(0.2, 3.0)
            graphComponent.viewport.viewPosition = java.awt.Point(0, 0)
        }
    }

    private fun setupInteractions() {
        graphComponent.graphControl.addMouseWheelListener { e: MouseWheelEvent ->
            if (e.isControlDown || e.isMetaDown) {
                if (e.wheelRotation < 0) {
                    graphComponent.zoomIn()
                } else {
                    graphComponent.zoomOut()
                }
                e.consume()
            }
        }

        graphComponent.graphControl.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val cell = graphComponent.getCellAt(e.x, e.y) ?: return
                val value = graph.model.getValue(cell)
                if (value is ApiNode) {
                    onNodeSelected?.invoke(value)
                }
            }

            override fun mousePressed(e: MouseEvent) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e) || javax.swing.SwingUtilities.isMiddleMouseButton(e)) {
                    graphComponent.isPanning = true
                }
            }
        })

        graphComponent.graphControl.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val cell = graphComponent.getCellAt(e.x, e.y)
                graphComponent.graphControl.cursor = if (cell != null) {
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                } else {
                    Cursor.getDefaultCursor()
                }
            }
        })
    }
}
