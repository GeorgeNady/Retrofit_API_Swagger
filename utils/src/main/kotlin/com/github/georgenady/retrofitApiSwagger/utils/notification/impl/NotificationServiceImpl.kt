package com.github.georgenady.retrofitApiSwagger.utils.notification.impl

import com.github.georgenady.retrofitApiSwagger.utils.notification.NotificationActionItem
import com.github.georgenady.retrofitApiSwagger.utils.notification.NotificationService
import com.github.georgenady.retrofitApiSwagger.utils.notification.NotificationService.Companion.DEFAULT_NOTIFICATION_GROUP_ID
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * Default implementation of [NotificationService] using IntelliJ Platform APIs.
 */
class NotificationServiceImpl(private val project: Project) : NotificationService {

    override fun showNotification(
        title: String,
        content: String,
        type: NotificationType,
        actions: List<NotificationActionItem>,
        groupId: String?
    ) {
        ApplicationManager.getApplication().invokeLater {
            try {
                val targetGroupId = groupId ?: DEFAULT_NOTIFICATION_GROUP_ID
                val notificationGroup = NotificationGroupManager.getInstance()
                    .getNotificationGroup(targetGroupId)

                val notification = notificationGroup?.createNotification(title, content, type)
                    ?: return@invokeLater

                actions.forEach { item ->
                    notification.addAction(NotificationAction.createSimple(item.title) {
                        if (item.expireNotification) {
                            notification.expire()
                        }
                        item.action()
                    })
                }

                notification.notify(project)
            } catch (_: Throwable) {
                // Graceful fallback if notification manager is unavailable in testing or headless mode
            }
        }
    }

    override fun showInfo(
        title: String,
        content: String,
        actions: List<NotificationActionItem>,
        groupId: String?
    ) {
        showNotification(title, content, NotificationType.INFORMATION, actions, groupId)
    }

    override fun showWarning(
        title: String,
        content: String,
        actions: List<NotificationActionItem>,
        groupId: String?
    ) {
        showNotification(title, content, NotificationType.WARNING, actions, groupId)
    }

    override fun showError(
        title: String,
        content: String,
        actions: List<NotificationActionItem>,
        groupId: String?
    ) {
        showNotification(title, content, NotificationType.ERROR, actions, groupId)
    }

    override fun showConfirmationDialog(
        title: String,
        message: String,
        okText: String,
        cancelText: String,
        onConfirm: () -> Unit
    ) {
        ApplicationManager.getApplication().invokeLater {
            val result = Messages.showYesNoDialog(
                project,
                message,
                title,
                okText,
                cancelText,
                Messages.getWarningIcon()
            )
            if (result == Messages.YES) {
                onConfirm()
            }
        }
    }

    override fun showMessageDialog(
        title: String,
        message: String,
        isWarning: Boolean
    ) {
        ApplicationManager.getApplication().invokeLater {
            if (isWarning) {
                Messages.showWarningDialog(project, message, title)
            } else {
                Messages.showInfoMessage(project, message, title)
            }
        }
    }
}
