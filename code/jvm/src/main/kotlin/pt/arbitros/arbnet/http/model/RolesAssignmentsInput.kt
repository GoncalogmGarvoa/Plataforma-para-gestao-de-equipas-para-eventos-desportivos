package pt.arbitros.arbnet.http.model

data class DayAssignmentInput(
    val matchDayId: Int,
    val participantId: Int,
)

data class RoleAssignmentsInput(
    val role: String,
    val assignments: List<DayAssignmentInput>,
)
