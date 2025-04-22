package pt.arbitros.arbnet.http.model
import java.time.LocalTime

data class MatchDaySessionsInput(
    val matchDay: Int,
    val sessions: List<LocalTime>,
)

// (LocalTime.of(9, 0)),
// (LocalTime.of(15, 30))
