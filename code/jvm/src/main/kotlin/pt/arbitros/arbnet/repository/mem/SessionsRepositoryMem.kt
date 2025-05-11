package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.Session
import pt.arbitros.arbnet.repository.SessionsRepository
import java.time.LocalTime

class SessionsRepositoryMem : SessionsRepository {
    private val sessions = mutableListOf<Session>()
    private var nextId = 1

    override fun createSession(
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime,
    ): Boolean {
        val session =
            Session(
                id = nextId++,
                competitionIdMatchDay = competitionId,
                matchDayId = matchDate,
                startTime = startTime,
                endTime = null,
            )
        sessions.add(session)
        return true
    }

    override fun updateSession(
        sessionId: Int,
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSessionByMatchId(id: Int): List<Session> {
        TODO("Not yet implemented")
    }
}
