package pt.arbitros.arbnet.domain

data class Participant(
    val callListId: Int,
    val matchDayId: Int,
    val competitionIdMatchDay: Int,
    val userId: Int,
    val functionId: Int,
    val confirmationStatus: String,
)

enum class ConfirmationStatus(val value : String) {
    WAITING("waiting"),
    ACCEPTED("accepted"),
    DECLINED("declined")
}
