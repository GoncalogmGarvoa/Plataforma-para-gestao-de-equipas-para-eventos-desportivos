package pt.arbitros.arbnet.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pt.arbitros.arbnet.http.model.ReportInputModel

@Document(collection = "Reports")
data class ReportMongo(
    @Id val id: String? = null,
    val reportType: String,
    val competitionId: Int,
    val sealed: Boolean = false,
    val coverSheet: CoverSheet,
    val register: ReportRegister,
    val refereeEvaluations: List<RefereeEvaluation>  // Lista direta de avaliações dos árbitros
){
    companion object {
        fun fromInputModel(input: ReportInputModel): ReportMongo {
            return ReportMongo(
                id = input.id, // pode ser null (criação) ou presente (‘update’)
                reportType = input.reportType,
                competitionId = input.competitionId,
                coverSheet = input.coverSheet,
                register = input.register,
                refereeEvaluations = input.refereeEvaluations
            )
        }
    }
}

data class CoverSheet(
    val style: String,
    val councilName: String,
    val sportsSeason: String,
    val year: Int,
    val month: Int,
    val authorName: String,
    val location: String,
    val numRounds: Int,
    val numSessions: Int,
    val sessions: List<SessionReportInfo>
)

data class SessionReportInfo(
    val sessionLabel: String,
    val date: String,           // "yyyy-MM-dd"
    val startTime: String,      // "HH:mm"
    val endTime: String,        // "HH:mm"
    val durationMinutes: Int
)

data class ReportRegister(
    val competitionPreparation: String,
    val competitionResults: String,
    val disqualifications: String,
    val courseOfCompetition: String,
    val otherObservations: String
)

data class RefereeEvaluation(
    val name: String,            // Nome do árbitro
    val category: String,        // Categoria
    val grade: Double,           // Nota (0.0 a 5.0, pode incluir .5)
    val notes: String            // Observações
)

