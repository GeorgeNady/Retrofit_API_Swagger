package com.github.georgenady.rettrofitapigraph.ui

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.github.georgenady.rettrofitapigraph.ui.components.ApiCardListContainer
import com.github.georgenady.rettrofitapigraph.ui.components.ApiEmptyStateView
import com.github.georgenady.rettrofitapigraph.ui.components.ApiStatusBarView
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JPanel

class ApiListPanel : JPanel(BorderLayout()) {

    var onRefreshRequested: (() -> Unit)? = null

    private val cardLayout = CardLayout()
    private val contentSwitcher = JPanel(cardLayout)

    private val statusBar = ApiStatusBarView()

    private val cardListContainer = ApiCardListContainer { selectedNode ->
        selectedNode.psiElement?.let { element ->
            if (element is com.intellij.pom.Navigatable && element.canNavigate()) {
                element.navigate(true)
            }
        }
    }

    private val emptyStateView = ApiEmptyStateView {
        onRefreshRequested?.invoke()
    }

    init {
        contentSwitcher.add(cardListContainer, "LIST")
        contentSwitcher.add(emptyStateView, "EMPTY")

        add(contentSwitcher, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)
    }

    fun setStatus(text: String) {
        statusBar.setMessage(text)
    }

    fun setEndpoints(endpoints: List<ApiNode>) {
        if (endpoints.isEmpty()) {
            cardLayout.show(contentSwitcher, "EMPTY")
            return
        }

        cardListContainer.render(endpoints)
        cardLayout.show(contentSwitcher, "LIST")
    }

    // Alias for backward compatibility
    fun updateData(endpoints: List<ApiNode>) {
        setEndpoints(endpoints)
    }
}