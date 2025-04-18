package pt.arbitros.arbnet.domain

data class SessionReferee(
    val sessionId: Int,
    val positionId: Int,
    val refereeId: Int,
    val matchDayIdSession: Int,
    val competitionIdMatchDay: Int
)
