package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.adaptable.Notification

interface NotificationRepository {

    fun createNotification(
        userId: Int,
        roleId: Int,
        message: String
    ): Int

    fun getNotificationsByUserAndRoleIds(userId: Int, roleId: Int): List<Notification>

    fun updateNotificationStatus(
        notificationId: Int,
    ): Boolean
}