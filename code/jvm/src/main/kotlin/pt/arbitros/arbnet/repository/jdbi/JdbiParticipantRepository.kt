package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.repository.ParticipantRepository

class JdbiParticipantRepository(
    private val handle: Handle,
) : ParticipantRepository {
    override fun addParticipant(participant: Participant): Boolean {
        TODO("Not yet implemented")
    }
}
