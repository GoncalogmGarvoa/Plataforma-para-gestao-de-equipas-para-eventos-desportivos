package pt.arbitros.arbnet.http.model

import java.time.LocalDate

class CallListOutputModel(
    val deadline: LocalDate,
    val callType: String,
    val councilId: Int,
    val competitionId: Int,
)