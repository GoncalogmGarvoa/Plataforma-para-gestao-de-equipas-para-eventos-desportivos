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
    val participant: List<String>, // todo list of String or list of Particpant
    val timeLine: LocalDate,
    val type: String,
    val matchDays: List<String>,
    val sessions: List<String>,
)
