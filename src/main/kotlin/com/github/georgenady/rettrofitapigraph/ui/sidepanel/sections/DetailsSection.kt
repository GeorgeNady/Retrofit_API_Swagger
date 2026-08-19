package com.github.georgenady.rettrofitapigraph.ui.sidepanel.sections

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.ui.sidepanel.SidePanelSection
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.Font
import java.awt.GridLayout
import javax.swing.BoxLayout
import javax.swing.JPanel

class DetailsSection : SidePanelSection {

    override val title: String = "API Details"
    
    private val nameLabel = JBLabel("Select an API...")
    private val methodLabel = JBLabel("")
    private val pathLabel = JBLabel("")
    private val classLabel = JBLabel("")
    
    private val annotationsLabel = JBLabel("Annotations:").apply { font = font.deriveFont(Font.BOLD, 12f) }
    private val annotationsList = JPanel().apply { 
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
    }

    private val parametersLabel = JBLabel("Parameters:").apply { font = font.deriveFont(Font.BOLD, 12f) }
    private val parametersList = JPanel().apply { 
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
    }

    override val component = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(10)
        isOpaque = false
        
        add(nameLabel.apply { font = font.deriveFont(Font.BOLD, 14f) })
        add(javax.swing.Box.createVerticalStrut(8))
        add(methodLabel)
        add(pathLabel)
        add(classLabel)
        
        add(javax.swing.Box.createVerticalStrut(12))
        add(annotationsLabel)
        add(annotationsList)
        
        add(javax.swing.Box.createVerticalStrut(12))
        add(parametersLabel)
        add(parametersList)
        
        add(javax.swing.Box.createVerticalGlue())
    }

    override fun onNodeSelected(node: ApiNode?) {
        annotationsList.removeAll()
        parametersList.removeAll()
        
        if (node == null) {
            nameLabel.text = "Select an API..."
            methodLabel.text = ""
            pathLabel.text = ""
            classLabel.text = ""
            annotationsLabel.isVisible = false
            parametersLabel.isVisible = false
        } else {
            nameLabel.text = node.methodName
            methodLabel.text = "Method: ${node.httpMethod}"
            pathLabel.text = "Path: ${node.path}"
            classLabel.text = "Service: ${node.className}"
            
            annotationsLabel.isVisible = node.annotations.isNotEmpty()
            node.annotations.forEach { anno ->
                val args = anno.arguments.entries.joinToString(", ") { "${it.key}=${it.value}" }
                val label = JBLabel("@${anno.name}${if (args.isNotEmpty()) "($args)" else ""}")
                label.border = JBUI.Borders.empty(2, 4)
                annotationsList.add(label)
            }
            
            parametersLabel.isVisible = node.parameters.isNotEmpty()
            node.parameters.forEach { param ->
                val label = JBLabel("${param.name}: ${param.type}")
                label.border = JBUI.Borders.empty(2, 4)
                parametersList.add(label)
            }
        }
        
        component.revalidate()
        component.repaint()
    }
}
