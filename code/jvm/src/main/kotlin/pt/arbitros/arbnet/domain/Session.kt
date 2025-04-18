package pt.arbitros.arbnet.domain

import java.time.LocalTime

data class Session(
    val id: Int,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val matchDayId: Int,
    val competitionIdMatchDay: Int
)
