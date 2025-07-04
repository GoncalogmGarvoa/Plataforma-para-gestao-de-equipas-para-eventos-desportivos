package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.adaptable.Notification

interface NotificationRepository {

    fun createNotification(
        userId: Int,
        message: String
    ): Int

    fun getNotificationsByUserId(userId: Int): List<Notification>

    fun updateNotificationStatus(
        notificationId: Int,
    ): Boolean
}