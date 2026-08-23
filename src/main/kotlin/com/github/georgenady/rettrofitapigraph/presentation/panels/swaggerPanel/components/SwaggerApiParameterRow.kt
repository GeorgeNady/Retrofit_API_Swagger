package com.github.georgenady.rettrofitapigraph.presentation.panels.swaggerPanel.components

import com.github.georgenady.rettrofitapigraph.domain.model.ApiNode
import com.github.georgenady.rettrofitapigraph.domain.model.ParameterLocation
import com.github.georgenady.rettrofitapigraph.domain.model.ParameterDetail
import com.github.georgenady.rettrofitapigraph.domain.usecase.FindPsiClassUseCase
import com.github.georgenady.rettrofitapigraph.presentation.main.MainToolViewModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import com.intellij.psi.search.PsiShortNamesCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwaggerApiParameterRow(
    private val project: Project,
    private val node: ApiNode, // Added node to access the parent interface's package
    private val param: ParameterDetail
) : JPanel() {

    private var isDataLoaded = false
    private val mockArea = JBTextArea("Loading example...").apply {
        rows = 5
        font = Font(Font.MONOSPACED, Font.PLAIN, 10)
        isEditable = false
    }

    private val inputField = JBTextField().apply {
        putClientProperty("parameter_name", param.name)
        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        alignmentX = Component.LEFT_ALIGNMENT

        val headerPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            alignmentX = Component.LEFT_ALIGNMENT

            val nameLabel = JBLabel(param.name).apply { font = font.deriveFont(Font.BOLD, 11f) }

            val typePrefix = JBLabel("   Type: ").apply { font = font.deriveFont(11f) }
            val typeLabel = JBLabel("<html><a href=''>${param.type}</a></html>").apply {
                font = font.deriveFont(11f)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                toolTipText = "Navigate to ${param.type}"

                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        // Pass the MouseEvent so we know where to show the balloon
                        navigateToType(param.fqn ?: param.type, e)
                    }
                })
            }

            add(nameLabel)
            add(typePrefix)
            add(typeLabel)
        }

        add(headerPanel)
        add(Box.createVerticalStrut(4))
        add(inputField)
        add(Box.createVerticalStrut(8))

        if (param.location == ParameterLocation.BODY && param.fqn != null) {
            val schemaLabel = JBLabel("Example Value | Schema:").apply {
                font = font.deriveFont(Font.ITALIC, 10f)
                alignmentX = Component.LEFT_ALIGNMENT
            }

            val schemaScroll = JScrollPane(mockArea).apply {
                preferredSize = Dimension(0, 80)
                alignmentX = Component.LEFT_ALIGNMENT
            }

            add(schemaLabel)
            add(Box.createVerticalStrut(2))
            add(schemaScroll)
            add(Box.createVerticalStrut(8))
        }
    }

    fun loadDataAsync() {
        if (isDataLoaded || param.location != ParameterLocation.BODY || param.fqn == null) return
        isDataLoaded = true

        val viewModel = project.service<MainToolViewModel>()
        viewModel.viewModelScope.launch(Dispatchers.Default) {
            val generateUseCase = com.github.georgenady.rettrofitapigraph.domain.usecase.GenerateJsonSchemaUseCase(project)
            val mockJson = try {
                generateUseCase(param.fqn!!)
            } catch (e: Exception) {
                "Error generating schema: ${e.message}"
            }

            withContext(Dispatchers.Main) {
                mockArea.text = mockJson
                mockArea.isEditable = true
            }
        }
    }


    private fun navigateToType(targetType: String, e: MouseEvent) {
        // Grab the ViewModel to get a CoroutineScope tied to your tool window's lifecycle
        val viewModel = project.service<MainToolViewModel>()

        viewModel.viewModelScope.launch {
            // 1. Run the heavy search in the background (Suspends without freezing UI)
            val findClassUseCase = FindPsiClassUseCase(project)
            val psiClass = findClassUseCase(targetType, node.className)

            // 2. Switch back to the UI thread (EDT) to update the UI
            withContext(Dispatchers.Main) {
                if (psiClass != null) {
                    psiClass.navigate(true)
                } else {
                    val cleanType = targetType.substringBefore("<").removeSuffix("?").trim()
                    showErrorBalloon(e, "Could not locate source file for <b>$cleanType</b>")
                }
            }
        }
    }

    /**
     * Displays a warning balloon at the location of the mouse click.
     */
    private fun showErrorBalloon(e: MouseEvent, message: String) {
        val balloon = JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(
                message,
                AllIcons.General.Warning,
                JBUI.CurrentTheme.Validator.warningBackgroundColor(),
                null
            )
            .setFadeoutTime(3000)
            .createBalloon()

        balloon.show(RelativePoint(e), Balloon.Position.above)
    }

    fun getParameterData(): Pair<String, String>? {
        val value = inputField.text ?: ""
        val name = inputField.getClientProperty("parameter_name") as? String ?: ""
        return if (name.isNotEmpty()) Pair(name, value) else null
    }
}