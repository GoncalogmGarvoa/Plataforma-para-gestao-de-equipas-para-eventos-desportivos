package pt.arbitros.arbnet.repository.Aux

import pt.arbitros.arbnet.domain.SessionReferee

interface SessionRefereesRepository {
    fun assignRefereeToSession(sessionReferee: SessionReferee): Boolean

    fun getRefereesBySession(
        sessionId: Int,
        matchDayId: Int,
        competitionId: Int,
    ): List<SessionReferee>

    fun removeRefereeFromSession(
        sessionId: Int,
        positionId: Int,
        refereeId: Int,
        matchDayIdSession: Int,
        competitionIdMatchDay: Int,
    ): Boolean
}
