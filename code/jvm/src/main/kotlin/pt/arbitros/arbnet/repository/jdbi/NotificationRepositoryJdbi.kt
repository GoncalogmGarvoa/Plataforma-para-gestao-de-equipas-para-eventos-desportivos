package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.adaptable.Notification
import pt.arbitros.arbnet.repository.NotificationRepository

class NotificationRepositoryJdbi (
    private val handle: Handle,
) : NotificationRepository {

    override fun createNotification(userId: Int, roleId: Int, message: String): Int =
        handle
            .createUpdate(
                """insert into dbp.notification (user_id, role_id, message) values (:userId, :roleId, :message)""",
            )
            .bind("userId", userId)
            .bind("roleId", roleId)
            .bind("message", message)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single() as Int

    override fun getNotificationsByUserAndRoleIds(
        userId: Int,
        roleId: Int
    ): List<Notification> =
        handle
            .createQuery(
                """select distinct * from dbp.notification where user_id = :userId and role_id = :roleId""",
            )
            .bind("userId", userId)
            .bind("roleId", roleId)
            .mapTo<Notification>()
            .list()

    override fun updateNotificationStatus(notificationId: Int): Boolean =
        handle
            .createUpdate(
                """update dbp.notification set read_status = true where id = :notificationId""",
            )
            .bind("notificationId", notificationId)
            .execute() > 0
}
