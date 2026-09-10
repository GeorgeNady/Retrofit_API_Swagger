package com.github.georgenady.retrofitApiSwagger.core.configuration

import com.github.georgenady.retrofitApiSwagger.data.service.SwaggerSettingsService
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.TitledSeparator
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.ListTableModel
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class SwaggerSettingsConfigurable(
    private val project: Project
) : Configurable {

    private val settingsService get() = SwaggerSettingsService.getInstance(project)

    // --- Base URL UI ---
    private var baseUrlField: JBTextField? = null

    // --- Scanner Settings UI ---
    private var scanKotlinCheckBox: JBCheckBox? = null
    private var scanJavaCheckBox: JBCheckBox? = null

    // --- Headers Table Data Model ---
    data class HeaderEntry(var key: String, var value: String)

    private class KeyColumn : ColumnInfo<HeaderEntry, String>("Header Name (Key)") {
        override fun valueOf(item: HeaderEntry): String = item.key
        override fun isCellEditable(item: HeaderEntry): Boolean = true
        override fun setValue(item: HeaderEntry, value: String) {
            item.key = value.trim()
        }
    }

    private class ValueColumn : ColumnInfo<HeaderEntry, String>("Header Value") {
        override fun valueOf(item: HeaderEntry): String = item.value
        override fun isCellEditable(item: HeaderEntry): Boolean = true
        override fun setValue(item: HeaderEntry, value: String) {
            item.value = value.trim()
        }
    }

    private val tableModel = ListTableModel<HeaderEntry>(KeyColumn(), ValueColumn())
    private val table = JBTable(tableModel)

    override fun getDisplayName(): String = "Retrofit API Swagger"

    override fun createComponent(): JComponent {

        // Base URL Input
        baseUrlField = JBTextField(settingsService.state.baseUrl)

        // Scanner Checkboxes
        scanKotlinCheckBox = JBCheckBox("Scan Kotlin files (*.kt)", settingsService.state.scanKotlinFiles)
        scanJavaCheckBox = JBCheckBox("Scan Java files (*.java)", settingsService.state.scanJavaFiles)

        // Headers Table Setup
        table.setShowGrid(true)
        table.emptyText.text = "No default headers configured. Click '+' to add one."

        val headersTablePanel = ToolbarDecorator.createDecorator(table)
            .setAddAction {
                tableModel.addRow(HeaderEntry("Header-Name", "Header-Value"))
                val lastRow = tableModel.rowCount - 1
                table.setRowSelectionInterval(lastRow, lastRow)
            }
            .setRemoveAction {
                val selectedRow = table.selectedRow
                if (selectedRow != -1) {
                    tableModel.removeRow(selectedRow)
                }
            }
            .disableUpDownActions()
            .createPanel().apply {
                // Set a reasonable height for the table inside the form
                preferredSize = Dimension(-1, 200)
            }

        // Build the Settings Screen with Sections
        return FormBuilder.createFormBuilder()
            // --- Section 1: Base URL ---
            .addComponent(TitledSeparator("Server Configuration"))
            .addLabeledComponent(
                JBLabel("Base URL:"),
                baseUrlField!!,
                1,
                false
            )
            .addTooltip("Default host prefix used for API execution (e.g., https://api.example.com or http://localhost:8080)")

            // --- Section 2: File Scanner Configuration ---
            .addComponent(TitledSeparator("File Scanner Configuration"))
            .addComponent(scanKotlinCheckBox!!)
            .addComponent(scanJavaCheckBox!!)

            // --- Section 3: Default Headers ---
            .addComponent(TitledSeparator("Global Request Headers"))
            .addComponent(headersTablePanel)

            // Fill remaining space vertically
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val savedState = settingsService.state

        // Check if Base URL changed
        val currentBaseUrl = baseUrlField?.text?.trim() ?: ""
        if (currentBaseUrl != savedState.baseUrl) return true

        // Check if Scanner settings changed
        if (scanKotlinCheckBox?.isSelected != savedState.scanKotlinFiles) return true
        if (scanJavaCheckBox?.isSelected != savedState.scanJavaFiles) return true

        // Check if Headers Table changed
        val currentHeadersInTable = tableModel.items
            .filter { it.key.isNotBlank() }
            .associate { it.key to it.value }

        return savedState.defaultHeaders != currentHeadersInTable
    }

    override fun apply() {

        // Save Base URL
        settingsService.state.baseUrl = baseUrlField?.text?.trim() ?: ""

        // Save Scanner Settings
        settingsService.state.scanKotlinFiles = scanKotlinCheckBox?.isSelected ?: true
        settingsService.state.scanJavaFiles = scanJavaCheckBox?.isSelected ?: true

        // Save Headers
        settingsService.clearHeaders()
        tableModel.items.forEach { entry ->
            if (entry.key.isNotBlank()) {
                settingsService.addDefaultHeader(entry.key, entry.value)
            }
        }
    }

    override fun reset() {
        val savedState = settingsService.state

        // Reset Base URL
        baseUrlField?.text = savedState.baseUrl

        // Reset Scanner Settings
        scanKotlinCheckBox?.isSelected = savedState.scanKotlinFiles
        scanJavaCheckBox?.isSelected = savedState.scanJavaFiles

        // Reset Headers Table
        val entries =
            savedState.defaultHeaders.map { HeaderEntry(it.key, it.value) }.toMutableList()
        tableModel.items = entries
    }

    override fun disposeUIResources() {
        baseUrlField = null
        scanKotlinCheckBox = null
        scanJavaCheckBox = null
    }
}