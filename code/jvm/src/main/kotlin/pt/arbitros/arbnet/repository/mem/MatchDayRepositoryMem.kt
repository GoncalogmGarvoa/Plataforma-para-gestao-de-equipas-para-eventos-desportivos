package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.MatchDay
import pt.arbitros.arbnet.repository.MatchDayRepository
import java.time.LocalDate

class MatchDayRepositoryMem : MatchDayRepository {
    private val matchDays = mutableListOf<MatchDayAux>()
    private var nextId = 1

    private data class MatchDayAux(
        val id: Int,
        val matchDate: LocalDate,
        val competitionId: Int,
    )

    override fun createMatchDay(
        competitionId: Int,
        matchDate: LocalDate,
    ): Int {
        val matchDay =
            MatchDayAux(
                id = nextId++,
                matchDate = matchDate,
                competitionId = competitionId,
            )
        matchDays.add(matchDay)
        return matchDay.id
    }

    override fun getMatchDayById(id: Int): MatchDay? {
        TODO("Not yet implemented")
    }

    override fun getMatchDayId(
        competitionId: Int,
        first: LocalDate,
    ): Int? {
        TODO("Not yet implemented")
    }
}
