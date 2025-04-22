package pt.arbitros.arbnet.repository

import java.time.LocalTime

interface RefereeRepository {
    fun getReferees(
        competitionId: Int,
        matchDate: Int,
        startTime: LocalTime,
    ): Boolean
}
