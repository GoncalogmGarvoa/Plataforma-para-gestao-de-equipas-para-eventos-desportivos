package pt.arbitros.arbnet.domain

import pt.arbitros.arbnet.http.model.MatchDaySessionsInput
import pt.arbitros.arbnet.http.model.ParticipantChoice
import java.time.LocalDate

class Event (
    val competitionName: String,
    val address: String,
    val phoneNumber: String,
    val email: String,
    val association: String,
    val location: String,
    val userId: Int,
    val participants: List<Participant>?,
    val deadline: LocalDate,
    val callListType: String,
    val matchDaySessions: List<MatchDaySessionsInput>,
)