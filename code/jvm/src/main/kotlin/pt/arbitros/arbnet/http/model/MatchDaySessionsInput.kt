package pt.arbitros.arbnet.http.model
import java.time.LocalDate
import java.time.LocalTime

data class MatchDaySessionsInput(
    val matchDay: LocalDate,
    val sessions: List<LocalTime>,
)

// (LocalTime.of(9, 0)),
// (LocalTime.of(15, 30))
