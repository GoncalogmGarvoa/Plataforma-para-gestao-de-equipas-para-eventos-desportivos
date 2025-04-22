package pt.arbitros.arbnet.repository

import java.time.LocalTime

interface SessionsRepository {
    fun createSession(
        competitionId: Int,
        matchDate: String,
        startTime: LocalTime,
    ): Boolean
}
