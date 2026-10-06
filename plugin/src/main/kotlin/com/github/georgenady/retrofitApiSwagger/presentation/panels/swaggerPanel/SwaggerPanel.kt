package com.github.georgenady.retrofitApiSwagger.presentation.panels.swaggerPanel

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.presentation.main.MainToolViewModel
import com.github.georgenady.retrofitApiSwagger.presentation.panels.graphPanel.WebviewResourceService
import com.github.georgenady.retrofitApiSwagger.utils.PluginUiUtils
import com.github.georgenady.retrofitApiSwagger.utils.notification.NotificationActionItem
import com.github.georgenady.retrofitApiSwagger.utils.notification.notificationService
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class SwaggerPanel(
    val project: Project,
    val isEditorMode: Boolean = false,
    val targetFile: VirtualFile? = null,
    val initialViewMode: String = if (isEditorMode) "list" else "graph"
) : JPanel(BorderLayout()) {

    private val viewModel = project.service<MainToolViewModel>()
    private val resourceService = project.service<WebviewResourceService>()

    // Uses JcefWebviewAdapter to isolate JCEF class loading completely from SwaggerPanel
    private var adapter: JcefWebviewAdapter? = null
    private var subscriptionJob: Job? = null

    init {
        if (!tryInitializeBrowser()) {
            add(createJcefMissingPanel(), BorderLayout.CENTER)
            notifyMissingJcef()
        }
    }

    fun isJcefAvailable(): Boolean = adapter != null

    private fun tryInitializeBrowser(): Boolean {
        if (!JcefWebviewAdapter.isJcefAvailable()) return false
        return try {
            val newAdapter = JcefWebviewAdapter(
                project = project,
                viewModel = viewModel,
                resourceService = resourceService,
                isEditorMode = isEditorMode,
                targetFile = targetFile,
                initialViewMode = initialViewMode
            )
            if (!newAdapter.isRealBrowserInitialized()) {
                return false
            }
            adapter = newAdapter
            removeAll()
            add(newAdapter.component, BorderLayout.CENTER)
            revalidate()
            repaint()
            subscribeToUpdates(newAdapter)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun subscribeToUpdates(a: JcefWebviewAdapter) {
        subscriptionJob?.cancel()
        subscriptionJob = viewModel.viewModelScope.launch(Dispatchers.Main) {
            viewModel.uiState
                .map { it.requestResults }
                .distinctUntilChanged()
                .collect { results ->
                    a.updateResponseResults(results)
                }
        }
    }

    private fun createJcefMissingPanel(): JPanel {
        return JPanel(GridBagLayout()).apply {
            border = JBUI.Borders.empty(24)
            val gbc = GridBagConstraints().apply {
                gridx = 0
                gridy = GridBagConstraints.RELATIVE
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                insets = JBUI.insets(8, 0)
                anchor = GridBagConstraints.CENTER
            }

            // Warning Icon + Title
            val titleLabel = JBLabel(
                "Web Browser (JCEF) Required",
                AllIcons.General.WarningDialog,
                SwingConstants.CENTER
            ).apply {
                font = JBUI.Fonts.label().biggerOn(3.0f)
                horizontalAlignment = SwingConstants.CENTER
            }
            add(titleLabel, gbc)

            // Explanatory Text
            val descLabel = JBLabel(
                "<html><center><p style='width: 320px; line-height: 1.4;'>" +
                "Android Studio requires the <b>Web Browser (JCEF)</b> plugin to render the interactive React canvas and API response viewer." +
                "<br><br>" +
                "Please install or enable the requirement from the IDE Plugins settings." +
                "</p></center></html>",
                SwingConstants.CENTER
            ).apply {
                horizontalAlignment = SwingConstants.CENTER
            }
            add(descLabel, gbc)

            // Single Prominent CTA Action Button
            val ctaButton = JButton("Open Plugins to Install Requirement", AllIcons.Actions.Download).apply {
                addActionListener {
                    PluginUiUtils.openPluginsSettings(project)
                }
            }

            val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
                add(ctaButton)
            }
            add(buttonPanel, gbc)
        }
    }

    private fun notifyMissingJcef() {
        if (!hasNotifiedMissingJcef) {
            hasNotifiedMissingJcef = true
            project.notificationService.showWarning(
                title = "Web Browser (JCEF) Required",
                content = "Ktorfit & Retrofit Studio requires the 'Web Browser (JCEF)' plugin to display the interactive API graph and response viewer.",
                actions = listOf(
                    NotificationActionItem("Open Plugins") {
                        PluginUiUtils.openPluginsSettings(project)
                    }
                )
            )
        }
    }

    override fun addNotify() {
        super.addNotify()
        val a = adapter ?: return
        subscribeToUpdates(a)
    }

    override fun removeNotify() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        super.removeNotify()
    }

    fun render(endpoints: List<ApiNode>) {
        adapter?.render(endpoints)
    }

    fun setViewMode(mode: String) {
        adapter?.setViewMode(mode)
    }

    companion object {
        @Volatile
        private var hasNotifiedMissingJcef = false

        fun resetNotificationFlag() {
            hasNotifiedMissingJcef = false
        }
    }
}
