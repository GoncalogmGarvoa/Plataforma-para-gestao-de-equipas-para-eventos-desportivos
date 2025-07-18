package pt.arbitros.arbnet.http.model


class ParticipantUpdateInput(
    val days: List<matchDayConfirmation>,
    val callListId: Int
)




data class matchDayConfirmation(
    val dayId: Int,
    val status: Int
)
