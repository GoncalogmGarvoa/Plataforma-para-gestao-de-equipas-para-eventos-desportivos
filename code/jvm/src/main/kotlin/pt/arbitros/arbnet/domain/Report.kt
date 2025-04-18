package pt.arbitros.arbnet.domain

data class Report(
    val id: Int,
    val reportType: String,
    val competitionId: Int
)
