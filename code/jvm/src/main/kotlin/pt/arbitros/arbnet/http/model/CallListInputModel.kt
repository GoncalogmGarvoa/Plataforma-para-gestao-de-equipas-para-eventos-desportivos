package pt.arbitros.arbnet.http.model

import pt.arbitros.arbnet.http.model.CallListInputLike
import java.time.LocalDate
import java.time.LocalTime

class CallListInputModel(
    override val competitionName: String,
    override val address: String,
    override val phoneNumber: String,
    override val email: String,
    override val association: String,
    override val location: String,
    override val userId: Int,
    override val participants: List<ParticipantChoice>?,
    val deadline: LocalDate,
    override val callListType: String,
    val matchDaySessions: List<MatchDaySessionsInput>,
) : CallListInputLike

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
