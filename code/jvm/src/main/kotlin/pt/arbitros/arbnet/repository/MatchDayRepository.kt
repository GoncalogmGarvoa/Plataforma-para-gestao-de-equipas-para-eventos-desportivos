package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.MatchDay
import java.time.LocalDate

interface MatchDayRepository {
    fun createMatchDay(
        competitionId: Int,
        matchDate: LocalDate,
    ): Int

    fun updateMatchDay(
        id: Int?,
        competitionId: Int,
        matchDate: LocalDate,
    ): Int

    fun getMatchDayById(id: Int): MatchDay?

    fun getMatchDayId(
        competitionId: Int,
        first: LocalDate,
    ): Int?

    fun getMatchDaysByCompetition(competitionId: Int): List<MatchDay>

    fun deleteCompetitionMatchDays(competitionId: Int): Boolean
}
