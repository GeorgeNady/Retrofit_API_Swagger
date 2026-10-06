package com.github.georgenady.retrofitApiSwagger.utils.notification

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * Reusable notification and alerter service interface for IntelliJ Platform plugins.
 */
interface NotificationService {

    /**
     * Shows a balloon notification in the IDE.
     */
    fun showNotification(
        title: String,
        content: String,
        type: NotificationType = NotificationType.INFORMATION,
        actions: List<NotificationActionItem> = emptyList(),
        groupId: String? = null
    )

    /**
     * Shows an informational balloon notification.
     */
    fun showInfo(
        title: String,
        content: String,
        actions: List<NotificationActionItem> = emptyList(),
        groupId: String? = null
    )

    /**
     * Shows a warning balloon notification.
     */
    fun showWarning(
        title: String,
        content: String,
        actions: List<NotificationActionItem> = emptyList(),
        groupId: String? = null
    )

    /**
     * Shows an error balloon notification.
     */
    fun showError(
        title: String,
        content: String,
        actions: List<NotificationActionItem> = emptyList(),
        groupId: String? = null
    )

    /**
     * Shows a modal native confirmation/alerter dialog with CTA options.
     */
    fun showConfirmationDialog(
        title: String,
        message: String,
        okText: String = "OK",
        cancelText: String = "Cancel",
        onConfirm: () -> Unit
    )

    /**
     * Shows a native information or warning message dialog.
     */
    fun showMessageDialog(
        title: String,
        message: String,
        isWarning: Boolean = false
    )

    companion object {
        const val DEFAULT_NOTIFICATION_GROUP_ID = "Retrofit API Swagger Notification Group"

        fun getInstance(project: Project): NotificationService =
            project.service<NotificationService>()
    }
}
