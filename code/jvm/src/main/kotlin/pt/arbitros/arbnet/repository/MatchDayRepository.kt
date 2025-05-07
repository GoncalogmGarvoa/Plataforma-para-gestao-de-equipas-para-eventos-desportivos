package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.MatchDay
import java.time.LocalDate

interface MatchDayRepository {
    fun createMatchDay(
        competitionId: Int,
        matchDate: LocalDate,
    ): Int

    fun getMatchDayById(id: Int): MatchDay?

    fun getMatchDayId(
        competitionId: Int,
        first: LocalDate,
    ): Int?

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
