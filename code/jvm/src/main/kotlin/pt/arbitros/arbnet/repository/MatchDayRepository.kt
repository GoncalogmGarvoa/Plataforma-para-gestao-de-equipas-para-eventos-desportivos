package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.MatchDay

interface MatchDayRepository {
    fun createMatchDay(
        competitionId: Int,
        matchDate: String,
    ): Boolean

    fun findMatchDayById(
        id: Int,
        competitionId: Int,
    ): MatchDay?

    fun getMatchDaysByCompetition(competitionId: Int): List<MatchDay>

    fun deleteMatchDay(
        id: Int,
        competitionId: Int,
    ): Boolean

    fun updateMatchDay(matchDay: MatchDay): Boolean
}
