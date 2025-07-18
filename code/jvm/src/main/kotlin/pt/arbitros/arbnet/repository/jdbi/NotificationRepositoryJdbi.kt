package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.adaptable.Notification
import pt.arbitros.arbnet.repository.NotificationRepository

class NotificationRepositoryJdbi (
    private val handle: Handle,
) : NotificationRepository {

    override fun createNotification(userId: Int, message: String): Int =
        handle
            .createUpdate(
                """insert into dbp.notification (user_id, message) values (:userId, :message)""",
            )
            .bind("userId", userId)
            .bind("message", message)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single() as Int

    override fun getNotificationsByUserId(
        userId: Int
    ): List<Notification> =
        handle
            .createQuery(
                """select distinct * from dbp.notification where user_id = :userId""",
            )
            .bind("userId", userId)
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
