package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Session
import java.time.LocalTime

interface SessionsRepository {
    fun createSession(
        competitionId: Int,
        matchDateId: Int,
        startTime: LocalTime,
    ): Boolean

    fun updateSession(
        sessionId: Int,
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime,
    ): Boolean

    fun getSessionByMatchId(id: Int): List<Session>

    fun getSessionById(id: Int): Session?

    fun finishSession(id: Int): Boolean

    fun getSessionsByCompetitionId(competitionId: Int): List<Session>

    fun deleteCompetitionSessions(competitionId: Int): Boolean

    fun setEndTime(
        sessionId: Int,
        endTime: LocalTime,
    ): Boolean
}
