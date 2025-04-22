package pt.arbitros.arbnet.repository

interface MatchDayRepository {
    fun createMatchDay(
        competitionId: Int,
        matchDate: Int,
    ): Int

//    fun findMatchDayById(
//        id: Int,
//        competitionId: Int,
//    ): MatchDay?
//
//    fun getMatchDaysByCompetition(competitionId: Int): List<MatchDay>
//
//    fun deleteMatchDay(
//        id: Int,
//        competitionId: Int,
//    ): Boolean
//
//    fun updateMatchDay(matchDay: MatchDay): Boolean
}
