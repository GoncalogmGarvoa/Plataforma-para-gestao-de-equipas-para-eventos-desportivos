package pt.arbitros.arbnet.http.model.report

import pt.arbitros.arbnet.domain.CoverSheet
import pt.arbitros.arbnet.domain.JurySheet
import pt.arbitros.arbnet.domain.RefereeEvaluation

data class ReportInputModel(
    val id: String? = null,                        // Opcional: usado em ‘updates’
    val reportType: String,
    val competitionId: Int,
    val coverSheet: CoverSheet,
    val register: Map<String, String> = emptyMap(), // Mapa de registos com chave como nome do registo e conteudo do registo
    val refereeEvaluations: List<RefereeEvaluation>,
    val jury: List<JurySheet> // Lista de folhas de júri, se aplicável
)