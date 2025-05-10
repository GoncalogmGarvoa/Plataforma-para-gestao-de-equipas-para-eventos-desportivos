package pt.arbitros.arbnet.http.model

import java.time.LocalDate

class CallListInputModel(
    val competitionName: String,
    val address: String,
    val phoneNumber: String,
    val email: String,
    val association: String,
    val location: String,
    val userId: Int,
    val participants: List<ParticipantChoice>,
    val deadline: LocalDate,
    val callListType: String,
    val matchDaySessions: List<MatchDaySessionsInput>,
)

data class ParticipantChoice(
    val userId: Int,
    val functionByMatchDay: List<FunctionByMatchDayDto>,
)

data class FunctionByMatchDayDto(
    val matchDay: LocalDate,
    val function: String,
)
