package pt.arbitros.arbnet.repositoryJdbi

import org.jdbi.v3.core.Handle
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
                """update dbp.participant set role = :roleId where id = :participantId and match_day_id = :matchDayId""",
            ).bind("roleId", roleId)
            .bind("participantId", participantId)
            .bind("matchDayId", matchDayId)
            .execute() > 0
}
