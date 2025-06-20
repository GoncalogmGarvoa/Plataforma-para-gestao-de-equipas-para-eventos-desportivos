package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.ReportInputModel
import pt.arbitros.arbnet.repository.mongo.ReportMongoRepository
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo

@Component
class ReportService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val reportMongoRepository: ReportMongoRepository,
    private val utilsDomain: UtilsDomain,
) {

    fun createReport(
        report: ReportInputModel,
    ): Either<ApiError, ReportMongo> {
        return transactionManager.run {

            if (it.competitionRepository.getCompetitionById(report.competitionId) == null) {
                return@run failure(ApiError.InvalidField(
                    "Invalid competition ID",
                    "The provided competition ID does not exist or is invalid."
                ))
            }

            val reportMongo = ReportMongo.fromInputModel(report)

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
        else failure(ApiError.NotFound(
            "Report not found",
            "No report found with the provided ID."
        ))
    }

    fun updateReport(report: ReportInputModel): Either<ApiError, ReportMongo> {
        return transactionManager.run {
            if (report.id == null) {
                return@run failure(ApiError.NotFound(
                    "Report ID is required",
                    "The report ID must be provided to update a report."
                ))
            }

            if (it.competitionRepository.getCompetitionById(report.competitionId) == null) {
                return@run failure(ApiError.InvalidField(
                    "Invalid competition ID",
                    "The provided competition ID does not exist or is invalid."
                ))
            }

            val existingReport = reportMongoRepository.findById(report.id).orElse(null)
                ?: return@run failure(ApiError.NotFound(
                    "Report not found",
                    "No report found with the provided ID."
                ))

            if (existingReport.sealed) {
                return@run failure(ApiError.InvalidField(
                    "Report is sealed",
                    "Cannot update a sealed report."
                ))
            }

            val reportMongo = ReportMongo.fromInputModel(report)

            val updatedReport = reportMongoRepository.save(reportMongo)
            return@run success(updatedReport)
        }
    }

    fun sealReport(id: String): Either<ApiError, ReportMongo> {
        return transactionManager.run {
            val report = reportMongoRepository.findById(id).orElse(null)
                ?: return@run failure(ApiError.NotFound(
                    "Report not found",
                    "No report found with the provided ID."
                ))

            if (report.sealed) {
                return@run failure(ApiError.InvalidField(
                    "Report already sealed",
                    "The report with ID $id is already sealed."
                ))
            }

            val success = reportMongoRepository.seal(id) // assumes returns Boolean

            if (!success) {
                return@run failure(ApiError.InternalServerError(
                    "Failed to seal report",
                    "An error occurred while trying to seal the report with ID $id."
                ))
            }

            it.reportRepository.createReport(
                id,
                report.reportType,
                report.competitionId
            )

            val updated = reportMongoRepository.findById(id).orElse(null)
                ?: return@run failure(ApiError.InternalServerError(
                    "Failed to create report representative",
                    "An internal error occurred while trying to create the report representative for the sealed report with ID $id."
                ))

            success(updated)
        }
    }

}