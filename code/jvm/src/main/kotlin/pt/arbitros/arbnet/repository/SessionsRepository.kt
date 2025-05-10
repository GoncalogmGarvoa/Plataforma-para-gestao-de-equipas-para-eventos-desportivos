package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Session
import java.time.LocalTime

interface SessionsRepository {
    fun createSession(
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime,
    ): Boolean

    fun getSessionByMatchId(id: Int): List<Session>
}
