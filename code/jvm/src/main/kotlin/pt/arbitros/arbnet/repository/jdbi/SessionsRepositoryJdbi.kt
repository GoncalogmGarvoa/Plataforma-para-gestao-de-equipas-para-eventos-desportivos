package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.Session
import pt.arbitros.arbnet.repository.SessionsRepository
import java.time.LocalTime

class SessionsRepositoryJdbi(
    private val handle: Handle,
) : SessionsRepository {
    override fun createSession(
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime,
    ): Boolean =
        handle
            .createUpdate(
                """insert into dbp.session (start_time, match_day_id, competition_id_match_day) values (:start_time, :match_date, :competition_id)""",
            ).bind("competition_id", competitionId)
            .bind("match_date", matchDate)
            .bind("start_time", startTime)
            .execute() > 0

    override fun updateSession(
        sessionId: Int,
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime
    ): Boolean =
        handle
            .createUpdate(
                """update dbp.session set start_time = :start_time, match_day_id = :match_date, competition_id_match_day = :competition_id where id = :session_id""",
            ).bind("session_id", sessionId)
            .bind("competition_id", competitionId)
            .bind("match_date", matchDate)
            .bind("start_time", startTime)
            .execute() > 0

    override fun getSessionByMatchId(matchDayId: Int): List<Session> =
        handle
            .createQuery(
                """
                SELECT * FROM dbp.session 
                WHERE match_day_id = :matchDayId
                """.trimIndent(),
            ).bind("matchDayId", matchDayId)
            .mapTo(Session::class.java)
            .list()

    override fun getSessionById(id: Int): Session? =
        handle
            .createQuery(
                """
                    SELECT * FROM dbp.session 
                    WHERE id = :id
                    """.trimIndent(),
            ).bind("id", id)
            .mapTo<Session>()
            .singleOrNull()

    override fun finishSession(id: Int): Boolean =
        handle
            .createUpdate(
                """
                    UPDATE dbp.session 
                    SET end_time = now() 
                    WHERE id = :id
                    """.trimIndent(),
            ).bind("id", id)
            .execute() > 0
}
