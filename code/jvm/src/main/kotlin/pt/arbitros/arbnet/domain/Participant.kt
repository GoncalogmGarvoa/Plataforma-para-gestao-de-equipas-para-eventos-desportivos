package pt.arbitros.arbnet.domain

data class Participant(
    val callListId: Int,
    val matchDayId: Int,
    val competitionIdMatchDay: Int,
    val userId: Int,
    val functionId: Int,
    val confirmationStatus: ConfirmationStatus,
)

enum class ConfirmationStatus {
    WAITING,
    ACCEPTED,
    DECLINED,
}
