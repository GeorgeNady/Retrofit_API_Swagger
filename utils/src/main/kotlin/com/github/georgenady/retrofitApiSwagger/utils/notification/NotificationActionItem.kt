package com.github.georgenady.retrofitApiSwagger.utils.notification

/**
 * Encapsulates an actionable button for IDE notifications.
 */
data class NotificationActionItem(
    val title: String,
    val expireNotification: Boolean = true,
    val action: () -> Unit
)
