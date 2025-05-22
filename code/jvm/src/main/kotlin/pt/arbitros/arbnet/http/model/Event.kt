package pt.arbitros.arbnet.http.model

import pt.arbitros.arbnet.domain.MatchDay
import pt.arbitros.arbnet.domain.Participant
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
    val matchDaySessions: List<MatchDay>,
)