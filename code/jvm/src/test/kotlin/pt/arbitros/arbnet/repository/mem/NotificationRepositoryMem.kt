package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.adaptable.Notification
import pt.arbitros.arbnet.repository.NotificationRepository

class NotificationRepositoryMem : NotificationRepository {

    override fun createNotification(userId: Int, roleId: Int, message: String): Int {
        TODO("Not yet implemented")
    }

    override fun getNotificationsByUserAndRoleIds(
        userId: Int,
        roleId: Int
    ): List<Notification> {
        TODO("Not yet implemented")
    }

    override fun updateNotificationStatus(notificationId: Int): Boolean {
        TODO("Not yet implemented")
    }

}