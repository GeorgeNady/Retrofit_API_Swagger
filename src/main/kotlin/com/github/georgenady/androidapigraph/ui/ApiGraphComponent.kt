package com.github.georgenady.androidapigraph.ui

import com.github.georgenady.androidapigraph.model.ApiNode
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.BoxLayout

class ApiGraphComponent : JBPanel<ApiGraphComponent>(BorderLayout()) {

    private val container = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    init {
        add(JBScrollPane(container), BorderLayout.CENTER)
    }

    fun updateData(endpoints: List<ApiNode>) {
        container.removeAll()
        
        endpoints.groupBy { it.className }.forEach { (className, methods) ->
            val classPanel = JBPanel<JBPanel<*>>().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = BorderFactory.createTitledBorder(className)
                alignmentX = Component.LEFT_ALIGNMENT
            }
            
            methods.forEach { node ->
                val methodLabel = JBLabel("${node.httpMethod} ${node.path} (${node.methodName})").apply {
                    border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    
                    // Click to navigate
                    addMouseListener(object : java.awt.event.MouseAdapter() {
                        override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                            node.psiElement?.let { element ->
                                if (element is com.intellij.pom.Navigatable && element.canNavigate()) {
                                    element.navigate(true)
                                }
                            }
                        }
                    })
                }
                classPanel.add(methodLabel)
            }
            container.add(classPanel)
        }
        
        revalidate()
        repaint()
    }
}
