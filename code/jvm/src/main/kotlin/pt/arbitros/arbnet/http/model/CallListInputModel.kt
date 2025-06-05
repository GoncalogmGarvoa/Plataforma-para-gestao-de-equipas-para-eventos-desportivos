package pt.arbitros.arbnet.http.model

import java.time.LocalDate
import java.time.LocalTime

class CallListInputModel(
    val callListId: Int? = null,
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
    val equipments : List<String>,
)

data class ParticipantChoice(
    val userId: Int,
    val participantAndRole: List<FunctionByMatchDayDto>,
)

data class FunctionByMatchDayDto(
    val matchDay: LocalDate,
    val function: String,
)

data class MatchDaySessionsInput(
    val matchDay: LocalDate,
    val sessions: List<LocalTime>,
)

data class CallListIdInput(
    val id: Int,
)
