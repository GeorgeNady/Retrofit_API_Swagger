package com.github.georgenady.retrofitApiSwagger.utils.notification

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * Convenient accessor to get the [NotificationService] instance for a [Project].
 */
val Project.notificationService: NotificationService
    get() = this.service<NotificationService>()
