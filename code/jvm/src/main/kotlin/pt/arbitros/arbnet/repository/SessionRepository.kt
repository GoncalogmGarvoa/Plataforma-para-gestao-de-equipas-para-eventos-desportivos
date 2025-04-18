package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Session

interface SessionRepository {

    fun createSession(session: Session): Boolean

    fun findSessionById(id: Int, matchDayId: Int, competitionIdMatchDay: Int): Session?

    fun getSessionsByMatchDay(matchDayId: Int, competitionId: Int): List<Session>

    fun deleteSession(id: Int, matchDayId: Int, competitionId: Int): Boolean
}
