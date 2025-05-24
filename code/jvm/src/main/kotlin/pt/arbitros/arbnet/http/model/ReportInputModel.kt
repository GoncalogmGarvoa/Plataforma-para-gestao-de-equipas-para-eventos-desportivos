package pt.arbitros.arbnet.http.model

import pt.arbitros.arbnet.domain.CoverSheet
import pt.arbitros.arbnet.domain.RefereeEvaluation
import pt.arbitros.arbnet.domain.ReportRegister

data class ReportInputModel(
    val id: String? = null,                        // Opcional: usado em updates
    val reportType: String,
    val competitionId: Int,
    val coverSheet: CoverSheet,
    val register: ReportRegister,
    val refereeEvaluations: List<RefereeEvaluation>
)