package pt.arbitros.arbnet.http.model
import java.time.LocalTime

data class MatchDaySessionsInput(
    val matchDay: Int, // TODO review if change this to a proper date
    val sessions: List<LocalTime>,
)

// (LocalTime.of(9, 0)),
// (LocalTime.of(15, 30))
