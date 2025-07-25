package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.http.model.matchDayConfirmation
import pt.arbitros.arbnet.repository.ParticipantRepository

class ParticipantRepositoryJdbi(
    private val handle: Handle,
) : ParticipantRepository {
    override fun addParticipant(
        callListId: Int,
        matchDayId: Int,
        userId: Int,
        functionId: Int,
        competitionId: Int,
    ): Boolean =
        handle
            .createUpdate(
                """insert into dbp.participant (call_list_id, match_day_id, user_id, function_id, competition_id_match_day, role_id, confirmation_status) 
               values (:call_list_id, :match_day_id, :user_id, :function_id, :competition_id_match_day, :role_id, :confirmation_status)""",
            ).bind("call_list_id", callListId)
            .bind("match_day_id", matchDayId)
            .bind("user_id", userId)
            .bind("function_id", functionId)
            .bind("competition_id_match_day", competitionId)
            .bind("role_id", 0)
            .bind("confirmation_status", "waiting")
            .execute() > 0

    override fun updateParticipantRole(
        participantId: Int,
        functionId: Int,
        matchDayId: Int,
    ): Boolean =
        handle
            .createUpdate(
                """update dbp.participant set role_id = :roleId where referee_id = :participantId and match_day_id = :matchDayId""",
            ).bind("roleId", functionId)
            .bind("participantId", participantId)
            .bind("matchDayId", matchDayId)
            .execute() > 0

    override fun updateParticipantConfirmationStatus(
        days: List<matchDayConfirmation>,
        participantId: Int,
        callListId: Int
    ): Boolean {
        if (days.isEmpty()) {
            return handle
                .createUpdate(
                    """
                update dbp.participant
                set confirmation_status = 'declined'
                where call_list_id = :callListId
                  and user_id = :participantId
                """.trimIndent()
                )
                .bind("callListId", callListId)
                .bind("participantId", participantId)
                .execute() > 0
        }

        var rowsUpdated = 0
        for (day in days) {
            val statusText = if (day.status == 0) "declined" else "accepted"
            rowsUpdated += handle
                .createUpdate(
                    """
                update dbp.participant
                set confirmation_status = :status
                where call_list_id = :callListId
                  and user_id = :participantId
                  and match_day_id = :dayId
                """.trimIndent()
                )
                .bind("status", statusText)
                .bind("callListId", callListId)
                .bind("participantId", participantId)
                .bind("dayId", day.dayId)
                .execute()
        }

        return rowsUpdated > 0
    }



    override fun getParticipantsByCallList(callListId: Int): List<Participant> =
        handle
            .createQuery(
                """
                SELECT * FROM dbp.participant 
                WHERE call_list_id = :callListId
                """.trimIndent(),
            ).bind("callListId", callListId)
            .mapTo<Participant>()
            .list()

    override fun getParticipantById(participantId: Int): Participant? =
        handle
            .createQuery("""select * from dbp.participant where user_id = :participantId limit 1 """)
            .bind("participantId", participantId)
            .mapTo<Participant>()
            .firstOrNull()

    override fun isCallListDone(callListId: Int): Boolean {
        val count =
            handle
                .createQuery(
                    """
                    select count(*) from dbp.participant
                    where call_list_id = :callListId
                      and confirmation_status = 'waiting'
                    """.trimIndent(),
                ).bind("callListId", callListId)
                .mapTo<Int>()
                .single()
        return count == 0
    }

    override fun batchAddParticipants(participants: List<Participant>): Boolean {
        val sql = """
        insert into dbp.participant (
            call_list_id, match_day_id, competition_id_match_day,
            user_id, function_id, confirmation_status
        ) values (
            :call_list_id, :match_day_id,:competition_id_match_day,
            :user_id, :function_id, :confirmation_status
        )
        """

        val batch = handle.prepareBatch(sql)
        participants.forEach {
            batch
                .bind("call_list_id", it.callListId)
                .bind("match_day_id", it.matchDayId)
                .bind("competition_id_match_day", it.competitionIdMatchDay)
                .bind("user_id", it.userId)
                .bind("function_id", it.functionId)
                .bind("confirmation_status", it.confirmationStatus)
                .add()
        }

        return batch.execute().all { it > 0 }
    }

    override fun deleteParticipantsByCallListId(callListId: Int): Boolean {
        val sql = """
        delete from dbp.participant
        where call_list_id = :callListId
        """

        return handle.createUpdate(sql)
            .bind("callListId", callListId)
            .execute() > 0
    }
}
