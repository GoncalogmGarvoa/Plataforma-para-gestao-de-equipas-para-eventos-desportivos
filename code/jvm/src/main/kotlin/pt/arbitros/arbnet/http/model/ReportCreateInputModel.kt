package pt.arbitros.arbnet.http.model

data class ReportCreateInputModel(
    val reportType: String,
    val competitionId: Int,
    //TODO add other fields
)