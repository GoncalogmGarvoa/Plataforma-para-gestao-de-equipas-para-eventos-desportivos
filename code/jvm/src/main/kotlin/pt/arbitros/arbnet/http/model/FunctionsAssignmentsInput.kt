package pt.arbitros.arbnet.http.model

data class DayAssignmentInput(
    val matchDayId: Int,
    val participantId: Int,
)

data class FunctionsAssignmentsInput(
    val function: String,
    val assignments: List<DayAssignmentInput>,
)
