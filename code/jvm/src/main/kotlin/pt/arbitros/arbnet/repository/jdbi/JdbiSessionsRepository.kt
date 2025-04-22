package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.SessionsRepository
import java.time.LocalTime

class JdbiSessionsRepository(
    private val handle: Handle,
) : SessionsRepository {
    override fun createSession(
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime,
    ): Boolean {
        TODO("Not yet implemented")
    }
}
