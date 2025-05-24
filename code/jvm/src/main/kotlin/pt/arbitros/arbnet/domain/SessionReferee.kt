package pt.arbitros.arbnet.domain

data class SessionReferee(
    val sessionId: Int,
    val positionId: Int,
    val userId: Int,
    val matchDayIdSession: Int,
    val competitionIdMatchDay: Int
)
