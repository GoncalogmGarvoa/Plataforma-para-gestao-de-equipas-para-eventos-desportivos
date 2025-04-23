package pt.arbitros.arbnet.http.model

import java.time.LocalDate

class CallListInputModel(
    val competitionName: String,
    val competitionNumber: Int,
    val address: String,
    val phoneNumber: Int,
    val email: String,
    val association: String,
    val location: String,
    val councilId: Int,
    val participant: List<Int>,
    val deadLine: LocalDate,
    val matchDaySessions: List<MatchDaySessionsInput>,
)
