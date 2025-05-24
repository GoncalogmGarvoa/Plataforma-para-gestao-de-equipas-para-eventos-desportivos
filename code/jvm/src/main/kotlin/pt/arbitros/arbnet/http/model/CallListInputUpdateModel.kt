package pt.arbitros.arbnet.http.model

import pt.arbitros.arbnet.http.model.CallListInputLike
import java.time.LocalDate


class CallListInputUpdateModel (
    val callListId: Int,
    override val competitionName: String,
    override val address: String,
    override val phoneNumber: String,
    override val email: String,
    override val association: String,
    override val location: String,
    override val participants: List<ParticipantChoice>?,
    val deadline: LocalDate,
    override val callListType: String,
    override val userId: Int,
    val matchDaySessions: List<MatchDaySessionsInput>
) : CallListInputLike
