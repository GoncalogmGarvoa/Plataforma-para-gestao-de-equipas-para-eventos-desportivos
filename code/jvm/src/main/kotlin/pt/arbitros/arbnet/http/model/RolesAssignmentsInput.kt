package pt.arbitros.arbnet.http.model

data class DayAssignmentInput(
    val day: String, // or LocalDate
    val personId: Int,
)

data class RoleAssignmentsInput(
    val role: String,
    val assignments: List<DayAssignmentInput>,
)
