package pt.arbitros.arbnet.domain

import java.time.LocalDate

data class CallList(
    val id: Int,
    val deadline: LocalDate,
    val callType: String,
    val userId: Int,
    val competitionId: Int,
)

enum class CallListType(
    val callType: String,
) {
    CALL_LIST("callList"),
    SEALED_CALL_LIST("sealedCallList"),
    CONFIRMATION("confirmation"),
    FINAL_JURY("finalJury"),
}



data class CallListWithUserAndCompetition(
    val callListId: Int,
    val deadline: LocalDate,
    val callType: String,
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val competitionId: Int,
    val competitionName: String
)
