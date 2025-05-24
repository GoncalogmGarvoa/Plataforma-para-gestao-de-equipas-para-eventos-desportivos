package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.domain.SessionReferee
import pt.arbitros.arbnet.repository.auxiliary.SessionRefereesRepository

class SessionRefereesRepositoryJdbi(
    private val handle: Handle
) : SessionRefereesRepository {

    override fun updateSessionReferees(
        listSessionReferees: List<SessionReferee>
    ): Boolean {
        val batch = handle
            .prepareBatch(
                """
            INSERT INTO dbp.session_referees (
                session_id, position_id, user_id, match_day_id_session, competition_id_match_day
            )
            VALUES (:sessionId, :positionId, :userId, :matchDayIdSession, :competitionIdMatchDay)
            """
            )

        listSessionReferees.forEach { input ->
            batch
                .bind("sessionId", input.sessionId)
                .bind("positionId", input.positionId)
                .bind("userId", input.userId)
                .bind("matchDayIdSession", input.matchDayIdSession)
                .bind("competitionIdMatchDay", input.competitionIdMatchDay)
                .add()
        }

        val result = batch.execute()
        return result.any { it > 0 }
    }

}