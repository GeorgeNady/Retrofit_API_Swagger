package com.github.georgenady.retrofitApiSwagger.core.configuration

import com.github.georgenady.retrofitApiSwagger.data.service.EdgeActionSettingsService
import com.github.georgenady.retrofitApiSwagger.domain.edgeaction.model.ActionPlacement
import com.github.georgenady.retrofitApiSwagger.domain.edgeaction.model.EdgeActionConfig
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBSplitter
import com.intellij.ui.TitledSeparator
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import java.util.UUID
import javax.swing.*

class EdgeActionsConfigurable(
    private val project: Project
) : Configurable {

    private val settingsService get() = EdgeActionSettingsService.getInstance(project)

    private val listModel = DefaultListModel<EdgeActionConfig>()
    private val actionList = JBList(listModel)

    private val nameField = JBTextField()
    private val descField = JBTextField()
    private val placementCombo = ComboBox(ActionPlacement.values())
    private val templateArea = JBTextArea(12, 40).apply {
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        lineWrap = true
        wrapStyleWord = true
    }

    private var currentSelected: EdgeActionConfig? = null
    private var isUpdatingUi = false

    override fun getDisplayName(): String = "API Graph Edge Actions"

    override fun createComponent(): JComponent {
        val root = JPanel(BorderLayout())

        // Load initial actions from state
        listModel.clear()
        settingsService.state.actions.forEach {
            listModel.addElement(it.copy())
        }

        actionList.cellRenderer = ListCellRenderer { _, value, _, isSelected, _ ->
            JLabel(value?.name ?: "").apply {
                border = JBUI.Borders.empty(4, 6)
                isOpaque = true
                background = if (isSelected) UIManager.getColor("List.selectionBackground") else UIManager.getColor("List.background")
                foreground = if (isSelected) UIManager.getColor("List.selectionForeground") else UIManager.getColor("List.foreground")
            }
        }

        actionList.addListSelectionListener { e ->
            if (e.valueIsAdjusting || isUpdatingUi) return@addListSelectionListener
            saveCurrentFieldsToModel()
            val selected = actionList.selectedValue
            currentSelected = selected
            loadModelToFields(selected)
        }

        val listDecorator = ToolbarDecorator.createDecorator(actionList)
            .setAddAction {
                saveCurrentFieldsToModel()
                val newAction = EdgeActionConfig(
                    id = "custom_" + UUID.randomUUID().toString().take(8),
                    name = "New Edge Action",
                    description = "Custom edge action template",
                    icon = "⚡",
                    placement = ActionPlacement.ANNOTATE_TARGET,
                    template = "@CustomAction(target = \"\${target.name}\", source = \"\${source.name}\")"
                )
                listModel.addElement(newAction)
                actionList.setSelectedValue(newAction, true)
            }
            .setRemoveAction {
                val idx = actionList.selectedIndex
                if (idx != -1) {
                    listModel.remove(idx)
                    currentSelected = null
                    if (listModel.size() > 0) {
                        actionList.selectedIndex = minOf(idx, listModel.size() - 1)
                    } else {
                        clearFields()
                    }
                }
            }
            .createPanel()

        val editorPanel = FormBuilder.createFormBuilder()
            .addComponent(TitledSeparator("Edge Action Configuration"))
            .addLabeledComponent(JBLabel("Action Name:"), nameField, 1, false)
            .addLabeledComponent(JBLabel("Description:"), descField, 1, false)
            .addLabeledComponent(JBLabel("Target Placement:"), placementCombo, 1, false)
            .addComponent(TitledSeparator("Velocity Template"))
            .addComponentFillVertically(JBScrollPane(templateArea), 0)
            .addComponent(
                JBLabel(
                    "<html><small style='color:gray;'>" +
                        "Variables: <b>\${source.name}</b>, <b>\${source.methodName}</b>, <b>\${source.httpMethod}</b>, <b>\${source.path}</b>, <b>\${source.className}</b>, <b>\${source.returnType}</b><br/>" +
                        "Same properties available on <b>\${target}</b>." +
                        "</small></html>"
                )
            )
            .panel

        val splitter = JBSplitter(false, 0.35f).apply {
            firstComponent = listDecorator
            secondComponent = editorPanel
        }

        root.add(splitter, BorderLayout.CENTER)

        if (listModel.size() > 0) {
            actionList.selectedIndex = 0
            currentSelected = listModel.get(0)
            loadModelToFields(currentSelected)
        }

        return root
    }

    private fun saveCurrentFieldsToModel() {
        val selected = currentSelected ?: return
        selected.name = nameField.text.trim()
        selected.description = descField.text.trim()
        selected.placement = placementCombo.selectedItem as? ActionPlacement ?: ActionPlacement.ANNOTATE_TARGET
        selected.template = templateArea.text
    }

    private fun loadModelToFields(config: EdgeActionConfig?) {
        isUpdatingUi = true
        if (config != null) {
            nameField.text = config.name
            descField.text = config.description
            placementCombo.selectedItem = config.placement
            templateArea.text = config.template
            nameField.isEnabled = true
            descField.isEnabled = true
            placementCombo.isEnabled = true
            templateArea.isEnabled = true
        } else {
            clearFields()
        }
        isUpdatingUi = false
    }

    private fun clearFields() {
        nameField.text = ""
        descField.text = ""
        templateArea.text = ""
        nameField.isEnabled = false
        descField.isEnabled = false
        placementCombo.isEnabled = false
        templateArea.isEnabled = false
    }

    override fun isModified(): Boolean = true

    override fun apply() {
        saveCurrentFieldsToModel()
        val actions = mutableListOf<EdgeActionConfig>()
        for (i in 0 until listModel.size()) {
            actions.add(listModel.get(i).copy())
        }
        settingsService.state.actions = actions
    }

    override fun reset() {
        listModel.clear()
        settingsService.state.actions.forEach {
            listModel.addElement(it.copy())
        }
        if (listModel.size() > 0) {
            actionList.selectedIndex = 0
            currentSelected = listModel.get(0)
            loadModelToFields(currentSelected)
        } else {
            clearFields()
        }
    }
}
