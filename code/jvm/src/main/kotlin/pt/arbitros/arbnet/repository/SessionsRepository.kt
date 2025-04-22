package pt.arbitros.arbnet.repository

import java.time.LocalTime

interface SessionsRepository {
    fun createSession(
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime,
    ): Boolean
}
