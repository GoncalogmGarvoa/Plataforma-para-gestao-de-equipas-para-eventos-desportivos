package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.model.ReportInputModel
import pt.arbitros.arbnet.http.model.ReportUpdateInputModel
import pt.arbitros.arbnet.repository.ReportMongoRepository
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo

sealed class ReportError {
    data object NotFound : ReportError()
    data object AlreadyExists : ReportError()
    data object AlreadySealed : ReportError()
    data object InternalError : ReportError()
    data object InvalidCompetitionId : ReportError()
}

@Component
class ReportService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val reportMongoRepository: ReportMongoRepository,
    private val utilsDomain: UtilsDomain,
) {

    fun createReport(
        report: ReportInputModel,
    ): Either<ReportError, ReportMongo> {
        return transactionManager.run {

            if (it.competitionRepository.getCompetitionById(report.competitionId) == null) {
                return@run failure(ReportError.InvalidCompetitionId)
            }

            val reportMongo = ReportMongo.fromInputModel(report)

            val reportCreated = reportMongoRepository.save(reportMongo)

            return@run success(reportCreated)
        }
    }

    fun getAllReports(): Either<ReportError, List<ReportMongo>> {
        return success(reportMongoRepository.findAll())
    }

    fun getReportById(id: String): Either<ReportError, ReportMongo?> {
        val result = reportMongoRepository.findById(id)
        return if (result.isPresent) success(result.get())
        else failure(ReportError.NotFound)
    }

    fun updateReport(report: ReportInputModel): Either<ReportError, ReportMongo> {
        return transactionManager.run {
            if (report.id == null) {
                return@run failure(ReportError.NotFound)
            }

            if (it.competitionRepository.getCompetitionById(report.competitionId) == null) {
                return@run failure(ReportError.InvalidCompetitionId)
            }

            val existingReport = reportMongoRepository.findById(report.id).orElse(null)
                ?: return@run failure(ReportError.NotFound)

            if (existingReport.sealed) {
                return@run failure(ReportError.AlreadySealed)
            }

            val reportMongo = ReportMongo.fromInputModel(report)

            val updatedReport = reportMongoRepository.save(reportMongo)
            return@run success(updatedReport)
        }
    }

    fun sealReport(id: String): Either<ReportError, ReportMongo> {
        return transactionManager.run {
            val report = reportMongoRepository.findById(id).orElse(null)
                ?: return@run failure(ReportError.NotFound)

            if (report.sealed) {
                return@run failure(ReportError.AlreadySealed) // Already sealed
            }

            val success = reportMongoRepository.seal(id) // assumes returns Boolean

            if (!success) {
                return@run failure(ReportError.InternalError)
            }

            it.reportRepository.createReport(
                id,
                report.reportType,
                report.competitionId
            )

            val updated = reportMongoRepository.findById(id).orElse(null)
                ?: return@run failure(ReportError.InternalError)

            success(updated)
        }
    }

}