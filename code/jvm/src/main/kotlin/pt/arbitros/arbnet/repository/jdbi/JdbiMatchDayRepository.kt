package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.MatchDayRepository

class JdbiMatchDayRepository(
    private val handle: Handle,
) : MatchDayRepository {
    override fun createMatchDay(
        competitionId: Int,
        matchDate: Int,
    ): Int {
        TODO("Not yet implemented")
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
}
