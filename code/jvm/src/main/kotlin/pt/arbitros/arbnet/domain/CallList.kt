package pt.arbitros.arbnet.domain

import java.time.LocalDate

data class CallList(
    val id: Int,
    val deadline: LocalDate,
    val callType: String,
    val councilId: Int,
    val competitionId: Int,
)

enum class CallListType(val callType: String) { //TODO decidir o nome dos snapshots
    CALL_LIST("callList"),
    CONFIRMATION("confirmation"),
    FINAL_JURY("finalJury"),
}