package pt.arbitros.arbnet.http.model
import java.time.LocalDate
import java.time.LocalTime

data class MatchDaySessionsInput(
    val matchDay: LocalDate,
    val sessions: List<LocalTime>,
)

/*
"matchDaySessions": [
{
    "matchDay": "2025-05-21",
    "sessions": [
    "15:30"
    ]
},
{
    "matchDay": "2025-05-22",
    "sessions": [
    "09:00",
    "15:30"
    ]
},
{
    "matchDay": "2025-05-23",
    "sessions": [
    "09:00",
    "15:30"
    ]
}
]

 */
