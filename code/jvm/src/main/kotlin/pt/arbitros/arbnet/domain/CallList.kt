package pt.arbitros.arbnet.domain

import java.time.LocalDate

data class CallList(
    val id: Int,
    val deadline: LocalDate?,
    val callType: String?,
    val councilId: Int?,
    val competitionId: Int?
)
