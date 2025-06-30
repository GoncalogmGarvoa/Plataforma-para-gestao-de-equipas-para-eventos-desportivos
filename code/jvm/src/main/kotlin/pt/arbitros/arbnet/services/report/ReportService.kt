package pt.arbitros.arbnet.services.report

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.report.ReportInputModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.repository.mongo.ReportMongoRepository
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.report.ReportServiceInputValidation
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.success
import pt.arbitros.arbnet.transactionRepo

@Component
class ReportService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val reportMongoRepository: ReportMongoRepository,
    private val validationUtils: ReportServiceInputValidation,
    private val utilsDomain: UtilsDomain,
) {

    fun createReport(report: ReportInputModel): Either<ApiError, ReportMongo> {
        return transactionManager.run {

            val reportMongo = ReportMongo.Companion.fromInputModel(report)

            val validationResult = validationUtils.validateReportValues(
                reportMongo,
                createOrUpdate = true,
                it.competitionRepository,
                it.categoryRepository,
                it.functionRepository,
                it.sessionsRepository,
                it.matchDayRepository,
                it.positionRepository
            )

            if (validationResult is Failure) {
                return@run failure(validationResult.value)
            }

            val reportCreated = reportMongoRepository.save(reportMongo)

            return@run success(reportCreated)
        }
    }

    fun getAllReports(): Either<ApiError, List<ReportMongo>> {
        return success(reportMongoRepository.findAll())
    }

    fun getReportById(id: String): Either<ApiError, ReportMongo?> {
        val result = reportMongoRepository.findById(id)
        return if (result.isPresent) success(result.get())
        else failure(
            ApiError.NotFound(
                "Report not found",
                "No report found with the provided ID."
            )
        )
    }

    fun updateReport(report: ReportInputModel): Either<ApiError, ReportMongo> {
        return transactionManager.run {

            val reportMongo = ReportMongo.Companion.fromInputModel(report)

            val validationResult = validationUtils.validateReportValues(
                reportMongo,
                createOrUpdate = false,
                it.competitionRepository,
                it.categoryRepository,
                it.functionRepository,
                it.sessionsRepository,
                it.matchDayRepository,
                it.positionRepository
            )

            if (validationResult is Failure) {
                return@run failure(validationResult.value)
            }

            val existingReport = reportMongoRepository.findById(report.id!!).orElse(null)
                ?: return@run failure(
                    ApiError.NotFound(
                        "Report not found",
                        "No report found with the provided ID."
                    )
                )

            val updatedReport = reportMongoRepository.save(reportMongo)
            return@run success(updatedReport)
        }
    }

    fun sealReport(id: String): Either<ApiError, ReportMongo> {
        return transactionManager.run {
            val report = reportMongoRepository.findById(id).orElse(null)
                ?: return@run failure(
                    ApiError.NotFound(
                        "Report not found",
                        "No report found with the provided ID."
                    )
                )

            if (report.sealed) {
                return@run failure(
                    ApiError.InvalidField(
                        "Report already sealed",
                        "The report with ID $id is already sealed."
                    )
                )
            }

            val success = reportMongoRepository.seal(id,true) // assumes returns Boolean

            if (!success) {
                return@run failure(
                    ApiError.InternalServerError(
                        "Failed to seal report",
                        "An error occurred while trying to seal the report with ID $id."
                    )
                )
            }

            it.reportRepository.createReport(
                id,
                report.reportType,
                report.competitionId
            )

            val updated = reportMongoRepository.findById(id).orElse(null)
                ?: return@run failure(
                    ApiError.InternalServerError(
                        "Failed to create report representative",
                        "An internal error occurred while trying to create the report representative for the sealed report with ID $id."
                    )
                )

            success(updated)
        }
    }

    fun getAllReportsByCompetition(competitionId: Int): Either<ApiError, List<ReportMongo>> {
        val reports = reportMongoRepository.findByCompetitionId(competitionId, true).filterIsInstance<ReportMongo>()
        return success(reports)
    }

}