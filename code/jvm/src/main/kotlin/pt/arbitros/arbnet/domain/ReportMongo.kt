package pt.arbitros.arbnet.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pt.arbitros.arbnet.http.model.report.ReportInputModel

@Document(collection = "Reports")
data class ReportMongo(
    @Id val id: String? = null,
    val reportType: String,
    val competitionId: Int,
    val sealed: Boolean = false,
    val coverSheet: CoverSheet,
    val registers: Map<String, String>, // Mapa de registros com chave como nome do registo e conteudo do registo
    val refereeEvaluations: List<RefereeEvaluation>,  // Lista direta de avaliações dos árbitros
    val jury : List<JurySheet>// Lista de folhas de júri, se aplicável
){
    companion object {
        fun fromInputModel(input: ReportInputModel): ReportMongo {
            return ReportMongo(
                id = input.id, // pode ser null (criação) ou presente (‘update’)
                reportType = input.reportType,
                competitionId = input.competitionId,
                coverSheet = input.coverSheet,
                registers = input.register,
                refereeEvaluations = input.refereeEvaluations,
                jury = input.jury
            )
        }
    }
}

data class CoverSheet(
    val style: String,
    val councilName: String,
    val sportsSeason: String,
    val authorName: String,
    val location: String,
    val year: Int,
    val month: Int,
    val numMatchDays: Int,
    val numSessions: Int,
    val sessions: List<SessionReportInfo>
)

data class SessionReportInfo(
    val sessionId: Int,
    val date: String,           // "DD/MM/YYYY"
    val startTime: String,      // "HH:mm"
    val endTime: String,        // "HH:mm"
    val durationMinutes: Int
)

data class RefereeEvaluation(
    val name: String,            // Nome do árbitro
    val category: String,        // Categoria
    val grade: Int,           // Nota (0.0 a 5.0)
    val notes: String,            // Observações
    val functionBySession: Map< Int, String>? // Função por sessão, chave é o rótulo da sessão e valor é a função do árbitro naquela sessão
)

data class JurySheet (
    val matchDayId : Int, // ‘ID’ do dia de jogo
    val sessionId : Int, // ‘ID’ da sessão
    val juryMembers: List<JuryMember> // Lista de membros do júri
)

data class JuryMember(
    val position : String, // Função do membro do júri (ex: "Presidente", "Vogal")
    val juryMemberId : Int, // ‘ID’ do membro do júri
    val name : String, // Nome do membro do júri
    val category : String, // Categoria do membro do júri (ex: "Árbitro", "Delegado")
)