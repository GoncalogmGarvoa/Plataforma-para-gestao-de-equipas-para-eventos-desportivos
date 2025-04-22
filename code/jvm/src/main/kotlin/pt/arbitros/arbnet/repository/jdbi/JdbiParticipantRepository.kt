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
        role: String,
    ): Boolean {
        TODO("Not yet implemented")
    }
}
