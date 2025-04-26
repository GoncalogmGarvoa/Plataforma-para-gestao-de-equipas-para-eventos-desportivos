package pt.arbitros.arbnet.repository

import java.time.LocalDate

interface MatchDayRepository {
    fun createMatchDay(
        competitionId: Int,
        matchDate: LocalDate,
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
