package pt.arbitros.arbnet.domain

import java.time.LocalDate

data class MatchDay(
    val id: Int,
    val matchDate: LocalDate?,
    val competitionId: Int
)
