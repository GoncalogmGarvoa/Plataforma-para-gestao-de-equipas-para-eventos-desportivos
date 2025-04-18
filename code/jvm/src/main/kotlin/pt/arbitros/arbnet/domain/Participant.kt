package pt.arbitros.arbnet.domain

data class Participant(
    val callListId: Int,
    val matchDayId: Int,
    val competitionIdMatchDay: Int,
    val refereeId: Int,
    val roleId: Int,
    val confirmationStatus: ConfirmationStatus
)

enum class ConfirmationStatus {
    waiting, accepted, declined
}
