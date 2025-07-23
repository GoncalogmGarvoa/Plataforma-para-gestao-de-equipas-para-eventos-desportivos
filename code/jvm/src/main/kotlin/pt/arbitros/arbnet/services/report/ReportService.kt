package pt.arbitros.arbnet.services.report

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.ReportSQL
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.report.ReportInputModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.repository.mongo.ReportMongoRepository
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.report.validation.ReportValidator
import pt.arbitros.arbnet.services.report.validation.SealSqlPopulate
import pt.arbitros.arbnet.services.success
import pt.arbitros.arbnet.transactionRepo

@Component
class ReportService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val reportMongoRepository: ReportMongoRepository,
    private val validationUtils: ReportValidator,
    private val sqlPopulator : SealSqlPopulate,
    private val utilsDomain: UtilsDomain,
) {

    fun createReport(report: ReportInputModel): Either<ApiError, ReportMongo> {
        return transactionManager.run {

            val reportMongo = ReportMongo.Companion.fromInputModel(report)

            val validationResult = validationUtils.validate(
                reportMongo,
                createOrUpdate = true,
                it
            )

            if (validationResult is Failure) {
                return@run failure(validationResult.value)
            }

            val reportCreated: ReportMongo = reportMongoRepository.save(reportMongo)

            val saveReportInDb = it.reportRepository.createReport(
                reportCreated.id!!,
                reportCreated.reportType,
                reportCreated.competitionId
            )
            if (!saveReportInDb) {
                return@run failure(
                    ApiError.InternalServerError(
                        "Failed to create report in database",
                        "An internal error occurred while trying to create the report in the database."
                    )
                )
            }

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

            val validationResult = validationUtils.validate(
                reportMongo,
                createOrUpdate = false,
                it
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
                    ApiError.NotFound(
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

            sqlPopulator.reportSealSqlPopulate(updated, it)

            success(updated)
        }
    }

    fun getAllReportsByCompetition(competitionId: Int): Either<ApiError, List<ReportMongo>> {
        val reports = reportMongoRepository.findByCompetitionId(competitionId, true).filterIsInstance<ReportMongo>()
        return success(reports)
    }

    fun getAllReportsByType(reportType: String): Either<ApiError, List<ReportMongo>> {

        val reportsSql = transactionManager.run {
            it.reportRepository.getAllReportsByType(reportType)
        }

        val reportsMongo = reportsSql.map { reportSql ->
            reportMongoRepository.findById(reportSql.id).orElse(null)
        }.filterNotNull()

        return if (reportsMongo.isNotEmpty()) {
            success(reportsMongo)
        } else {
            failure(
                ApiError.NotFound(
                    "No reports found",
                    "No reports found for the specified type: $reportType."
                )
            )
        }
    }


}