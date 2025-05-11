package pt.arbitros.arbnet.domain

import java.time.LocalDate

data class MatchDay(
    val id: Int,
    val matchDate: LocalDate,
    val competitionId: Int,
    val sessions: List<Session>,
) {
    fun addSession(session: Session): MatchDay = this.copy(sessions = this.sessions + session)

    fun removeSession(session: Session): MatchDay = this.copy(sessions = this.sessions - session)
}

data class MatchDayDTO(
    val id: Int,
    val matchDate: LocalDate,
    val competitionId: Int,
)
