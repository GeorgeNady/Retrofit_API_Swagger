package com.github.georgenady.retrofitApiSwagger.utils

import com.github.georgenady.retrofitApiSwagger.utils.notification.NotificationActionItem
import com.github.georgenady.retrofitApiSwagger.utils.notification.NotificationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationServiceTest {

    @Test
    fun testNotificationActionItemExecution() {
        var clicked = false
        val action = NotificationActionItem(
            title = "Test Action",
            expireNotification = true,
            action = { clicked = true }
        )

        assertEquals("Test Action", action.title)
        assertTrue(action.expireNotification)
        action.action()
        assertTrue(clicked)
    }

    @Test
    fun testDefaultGroupId() {
        assertEquals(
            "Retrofit API Swagger Notification Group",
            NotificationService.DEFAULT_NOTIFICATION_GROUP_ID
        )
    }
}
