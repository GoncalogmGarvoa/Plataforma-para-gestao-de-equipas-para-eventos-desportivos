package pt.arbitros.arbnet.repositoryJdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.MatchDay
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

    override fun getMatchDayById(id: Int): MatchDay? =
        handle
            .createQuery("""select * from dbp.match_day where id = :id""")
            .bind("id", id)
            .mapTo<MatchDay>()
            .singleOrNull()
}



    //    override fun findMatchDayById(
//        id: Int,
//        competitionId: Int,
//    ): MatchDay? =
//        handle
//            .createQuery("""select * from dbp.match_day where id = :id and competition_id = :competition_id""")
//            .bind("id", id)
//            .bind("competition_id", competitionId)
//            .mapTo<MatchDay>()
//            .singleOrNull()
//
//    override fun getMatchDaysByCompetition(competitionId: Int): List<MatchDay> =
//        handle
//            .createQuery("""select * from dbp.match_day where competition_id = :competition_id""")
//            .bind("competition_id", competitionId)
//            .mapTo<MatchDay>()
//            .list()
//
//    override fun deleteMatchDay(
//        id: Int,
//        competitionId: Int,
//    ): Boolean =
//        handle
//            .createUpdate("""delete from dbp.match_day where id = :id and competition_id = :competition_id""")
//            .bind("id", id)
//            .bind("competition_id", competitionId)
//            .execute() > 0
//
//    override fun updateMatchDay(matchDay: MatchDay): Boolean =
//        handle
//            .createUpdate(
//                """update dbp.match_day set date = :date where id = :id and competition_id = :competition_id""",
//            ).bind("id", matchDay.id)
//            .bind("competition_id", matchDay.competitionId)
//            .bind("date", matchDay.matchDate)
//            .execute() > 0

