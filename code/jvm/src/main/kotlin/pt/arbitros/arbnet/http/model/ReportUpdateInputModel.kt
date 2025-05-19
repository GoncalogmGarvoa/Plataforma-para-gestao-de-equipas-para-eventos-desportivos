package pt.arbitros.arbnet.http.model

data class ReportUpdateInputModel(
    val id: String,
    val reportType: String,
    val competitionId: Int,
    //TODO add other fields
)