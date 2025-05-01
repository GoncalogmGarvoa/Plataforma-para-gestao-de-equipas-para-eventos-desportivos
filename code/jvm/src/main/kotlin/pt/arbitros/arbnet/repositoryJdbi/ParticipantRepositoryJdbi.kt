package pt.arbitros.arbnet.repositoryJdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.repository.ParticipantRepository

class ParticipantRepositoryJdbi(
    private val handle: Handle,
) : ParticipantRepository {
    override fun addParticipant(
        callListId: Int,
        matchDayId: Int,
        councilId: Int,
        competitionId: Int,
        refereeId: Int,
        roleId: Int,
    ): Boolean =
        handle
            .createUpdate(
                """insert into dbp.participant (call_list_id, match_day_id, council_id, competition_id_match_day, referee_id, role_id, confirmation_status) 
               values (:call_list_id, :match_day_id, :council_id, :competition_id_match_day, :referee_id, :role_id, :confirmation_status)""",
            ).bind("call_list_id", callListId)
            .bind("match_day_id", matchDayId)
            .bind("council_id", councilId)
            .bind("competition_id_match_day", competitionId)
            .bind("referee_id", refereeId)
            .bind("role_id", roleId)
            .bind("confirmation_status", "waiting")
            .execute() > 0

    override fun updateParticipantRole(
        participantId: Int,
        roleId: Int,
        matchDayId: Int,
    ): Boolean =
        handle
            .createUpdate(
                """update dbp.participant set role_id = :roleId where referee_id = :participantId and match_day_id = :matchDayId""",
            ).bind("roleId", roleId)
            .bind("participantId", participantId)
            .bind("matchDayId", matchDayId)
            .execute() > 0

    override fun updateParticipantConfirmationStatus(
        days: List<Int>,
        participantId: Int,
        callListId: Int
    ): Boolean = handle
        .createUpdate(
            """
        update dbp.participant
        set confirmation_status = case 
            when match_day_id in (<days>) then 'accepted'
            else 'declined'
        end
        where call_list_id = :callListId
          and referee_id = :participantId
        """.trimIndent()
        )
        .bind("callListId", callListId)
        .bind("participantId", participantId)
        .bindList("days", days)
        .execute() > 0

    override fun isCallListDone(callListId: Int): Boolean {
        val count = handle
            .createQuery(
                """
            select count(*) from dbp.participant
            where call_list_id = :callListId
              and confirmation_status = 'waiting'
        """.trimIndent(),
            )
            .bind("callListId", callListId)
            .mapTo<Int>()
            .single()
        return count == 0
    }

}

