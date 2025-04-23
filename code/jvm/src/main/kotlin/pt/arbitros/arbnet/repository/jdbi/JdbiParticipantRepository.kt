package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.ParticipantRepository

class JdbiParticipantRepository(
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
                """insert into dbp.participant (call_list_id, match_day_id, council_id, competition_id, referee_id, role, confirmation_status) 
               values (:call_list_id, :match_day_id, :council_id, :competition_id, :referee_id, :role, :confirmation_status)""",
            ).bind("call_list_id", callListId)
            .bind("match_day_id", matchDayId)
            .bind("council_id", councilId)
            .bind("competition_id", competitionId)
            .bind("referee_id", refereeId)
            .bind("role", roleId)
            .bind("confirmation_status", "waiting") // Define o valor padrão como "waiting"
            .execute() > 0
}
