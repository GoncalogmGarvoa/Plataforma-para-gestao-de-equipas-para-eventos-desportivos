package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.model.ReportCreateInputModel
import pt.arbitros.arbnet.http.model.ReportUpdateInputModel
import pt.arbitros.arbnet.repository.ReportMongoRepository
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo

sealed class ReportError {
    data object NotFound : ReportError()
    data object AlreadyExists : ReportError()
    data object AlreadySealed : ReportError()
    data object InternalError : ReportError()
}

@Component
class ReportService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val reportMongoRepository: ReportMongoRepository,
    private val utilsDomain: UtilsDomain,
) {

    fun createReport(
        report: ReportCreateInputModel,
    ): Either<ReportError, ReportMongo> {

        val reportMongo = ReportMongo(
            id = null,
            competitionId = report.competitionId,
            reportType = report.reportType,
            sealed = false
        )

        val reportCreated = reportMongoRepository.save(reportMongo)

        return success(reportCreated)
    }

    fun getAllReports(): Either<ReportError, List<ReportMongo>> {
        return success(reportMongoRepository.findAll())
    }

    fun getReportById(id: String): Either<ReportError, ReportMongo?> {
        val result = reportMongoRepository.findById(id)
        return if (result.isPresent) success(result.get())
        else failure(ReportError.NotFound)
    }

    fun updateReport(report: ReportUpdateInputModel): Either<ReportError, ReportMongo> {

        val existingReport = reportMongoRepository.findById(report.id).orElse(null)
            ?: return failure(ReportError.NotFound)

        if (existingReport.sealed) {
            return failure(ReportError.AlreadySealed)
        }

        val reportMongo = ReportMongo(
            id = report.id,
            competitionId = report.competitionId,
            reportType = report.reportType
        )

        val updatedReport = reportMongoRepository.save(reportMongo)
        return success(updatedReport)
    }

    fun sealReport(id: String): Either<ReportError, ReportMongo> {
        return transactionManager.run {
            val report = reportMongoRepository.findById(id).orElse(null)
                ?: return@run failure(ReportError.NotFound)

            if (report.sealed) {
                return@run failure(ReportError.InternalError) // Already sealed
            }

            val success = reportMongoRepository.seal(id) // assumes returns Boolean

            if (!success) {
                return@run failure(ReportError.InternalError)
            }

            val updated = reportMongoRepository.findById(id).orElse(null)
                ?: return@run failure(ReportError.InternalError)

            success(updated)
        }
    }

}