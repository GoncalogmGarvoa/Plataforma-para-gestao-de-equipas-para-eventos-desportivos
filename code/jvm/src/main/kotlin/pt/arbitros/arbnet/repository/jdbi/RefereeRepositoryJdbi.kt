package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.Referee
import pt.arbitros.arbnet.repository.RefereeRepository

class RefereeRepositoryJdbi(
    private val handle: Handle
) : RefereeRepository {

    override fun createReferee(userId: Int): Boolean =
        handle.createUpdate(
            """ INSERT INTO referee (user_id) VALUES (:userId) """
            )
            .bind("userId", userId)
            .execute() > 0

    override fun getAllReferees(referees: List<Int>): List<Referee> =
        handle
            .createQuery("SELECT user_id FROM dbp.referee WHERE user_id IN (<referees>)")
            .bindList("referees", referees)
            .mapTo<Referee>()
            .list()

    override fun deleteReferee(userId: Int): Boolean =
        handle.createUpdate(
            """ DELETE FROM referee WHERE user_id = :userId """
            )
            .bind("userId", userId)
            .execute() > 0
}