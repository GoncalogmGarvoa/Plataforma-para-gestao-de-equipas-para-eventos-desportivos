package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
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
}
