package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.MatchDay
import pt.arbitros.arbnet.domain.MatchDayDTO
import pt.arbitros.arbnet.domain.Session
import pt.arbitros.arbnet.repository.MatchDayRepository
import java.time.LocalDate

class MatchDayRepositoryJdbi(
    private val handle: Handle,
) : MatchDayRepository {
    override fun createMatchDay(
        competitionId: Int,
        matchDate: LocalDate,
    ): Int =
        handle
            .createUpdate(
                """insert into dbp.match_day (match_date, competition_id) values (:date, :competition_id)""",
            ).bind("competition_id", competitionId)
            .bind("date", matchDate)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single()

    override fun updateMatchDay(
        id: Int?,
        competitionId: Int,
        matchDate: LocalDate
    ): Int =
        handle
            .createUpdate(
                """update dbp.match_day set match_date = :date where id = :id and competition_id = :competition_id""",
            ).bind("id", id)
            .bind("competition_id", competitionId)
            .bind("date", matchDate)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single()

    override fun getMatchDayById(id: Int): MatchDay? =
        handle
            .createQuery("""select * from dbp.match_day where id = :id""")
            .bind("id", id)
            .mapTo<MatchDay>()
            .singleOrNull()

    override fun getMatchDayId(
        competitionId: Int,
        matchDate: LocalDate,
    ): Int? =
        handle
            .createQuery("""select id from dbp.match_day where competition_id = :cmId and match_date = :date""")
            .bind("cmId", competitionId)
            .bind("date", matchDate)
            .mapTo<Int>()
            .singleOrNull()

//    override fun getMatchDaysByCompetition(competitionId: Int): List<MatchDay> =
//        handle
//            .createQuery("""select * from dbp.match_day where competition_id = :competition_id""")
//            .bind("competition_id", competitionId)
//            .mapTo<MatchDay>()
//            .list()

    override fun getMatchDaysByCompetition(competitionId: Int): List<MatchDay> {
        val matchDays =
            handle
                .createQuery("SELECT * FROM dbp.match_day WHERE competition_id = :competition_id")
                .bind("competition_id", competitionId)
                .mapTo<MatchDayDTO>()
                .list()

        return matchDays.map { matchDay ->
            val sessions =
                handle
                    .createQuery(
                        """
                        SELECT * FROM dbp.session 
                        WHERE match_day_id = :match_day_id AND competition_id_match_day = :competition_id
                        """.trimIndent(),
                    ).bind("match_day_id", matchDay.id)
                    .bind("competition_id", competitionId)
                    .mapTo<Session>()
                    .list()

            MatchDay(
                id = matchDay.id,
                matchDate = matchDay.matchDate,
                competitionId = matchDay.competitionId,
                sessions = sessions,
            )
        }
    }

    override fun deleteCompetitionMatchDays(competitionId: Int): Boolean =
        handle
            .createUpdate(
                """delete from dbp.match_day where competition_id = :competition_id""",
            ).bind("competition_id", competitionId)
            .execute() > 0


}
