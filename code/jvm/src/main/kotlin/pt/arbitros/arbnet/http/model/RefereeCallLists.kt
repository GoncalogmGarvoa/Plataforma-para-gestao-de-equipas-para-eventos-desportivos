package pt.arbitros.arbnet.http.model

import java.time.LocalDate

class RefereeCallLists(
    val callListId: Int,
    val competitionId: Int,
    val competitionName: String,
    val address: String,
    val phoneNumber: String,
    val email: String,
    val association: String,
    val location: String,
    val deadline: LocalDate,
)