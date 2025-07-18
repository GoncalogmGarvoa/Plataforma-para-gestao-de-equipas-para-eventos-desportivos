package pt.arbitros.arbnet.http.model.calllist

import pt.arbitros.arbnet.domain.MatchDay


data class CallListReportOutputModel(
    val callListId: Int,
    val competitionId: Int,
    val competitionName: String,
    val matchDays: List<MatchDay>,
)

