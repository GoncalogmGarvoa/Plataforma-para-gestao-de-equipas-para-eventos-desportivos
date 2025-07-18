package pt.arbitros.arbnet.http.model.users

import pt.arbitros.arbnet.http.model.matchDayConfirmation


class ParticipantUpdateInputArbitrationCouncil(
    val days: List<matchDayConfirmation>,
    val callListId: Int,
    val userId: Int
)


