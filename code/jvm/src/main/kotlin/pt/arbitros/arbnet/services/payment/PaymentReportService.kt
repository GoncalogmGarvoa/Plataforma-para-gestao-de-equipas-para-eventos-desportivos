package pt.arbitros.arbnet.services.payment

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.CallListType
import pt.arbitros.arbnet.domain.Competition
import pt.arbitros.arbnet.domain.MatchDay
import pt.arbitros.arbnet.domain.PaymentReportMongo
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.calllist.CallListReportOutputModel
import pt.arbitros.arbnet.http.model.payment_report.PaymentReportInputModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.repository.mongo.PaymentReportMongoRepository
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.success
import pt.arbitros.arbnet.transactionRepo

@Component
class PaymentReportService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val paymentMongoRepository: PaymentReportMongoRepository,
    private val paymentReportCalculator: PaymentReportCalculator,
    private val utilsDomain: UtilsDomain,
) {

    fun getCallListsForPaymentByUserId(userId: Int,offset: Int, limit: Int) : Either<ApiError, List<CallListReportOutputModel>> =

        transactionManager.run { tx ->
            val callLists = tx.callListRepository.getCallListsByUserIdAndType(userId, CallListType.FINAL_JURY.callType,offset,limit)

            if (callLists.isEmpty()) {
                return@run failure(
                    ApiError.NotFound(
                        "No Call Lists found",
                        "No call lists found for the user with ID $userId."
                    )
                )
            }

            val callListsReport = callLists.map { callList ->
                val competition: Competition = tx.competitionRepository.getCompetitionById(callList.competitionId)
                    ?: return@run failure(
                        ApiError.NotFound(
                            "Competition not found",
                            "No competition found with ID ${callList.competitionId}"
                        )
                    )
                val matchDays: List<MatchDay> = tx.matchDayRepository.getMatchDaysByCompetition(callList.competitionId)

                CallListReportOutputModel(
                    callListId = callList.callListId,
                    competitionId = competition.competitionNumber,
                    competitionName = competition.name,
                    matchDays = matchDays
                )
            }
            success(callListsReport)
        }

    fun createPaymentReport(report: PaymentReportInputModel): Either<ApiError, PaymentReportMongo> {
        return transactionManager.run {
            val competitionRepository = it.competitionRepository //todo use this to validate competitionId

            val paymentListDetails = paymentReportCalculator.calculateTotalOwed(
                report,
                it
            )

            val reportMongo = PaymentReportMongo.Companion.fromInputModel(report, paymentListDetails)

            val result: PaymentReportMongo = paymentMongoRepository.save(reportMongo)


            it.reportRepository.createReport(
                result.id!!,
                result.reportType,
                result.competitionId
            )

            return@run success(result)
        }
    }

    fun getAllReportsByType(reportType: String): Either<ApiError, List<PaymentReportMongo>> {

        val reportsSql = transactionManager.run {
            it.reportRepository.getAllReportsByType(reportType)
        }

        val reportsMongo = reportsSql.mapNotNull { reportSql ->
            paymentMongoRepository.findById(reportSql.id).orElse(null)
        }

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

    fun getAllPaymentReports(): Either<ApiError, List<PaymentReportMongo>> {
        return success(paymentMongoRepository.findAll())
    }

    fun getPaymentReportById(id: String): Either<ApiError, PaymentReportMongo?> {
        val result = paymentMongoRepository.findById(id)
        return if (result.isPresent) success(result.get())
        else failure(
            ApiError.NotFound(
                "Payment Report not found",
                "No report found with the provided ID."
            )
        )
    }

    fun updatePaymentReport(report: PaymentReportInputModel): Either<ApiError, PaymentReportMongo> {
        return transactionManager.run {

            val competitionRepository = it.competitionRepository

            val paymentListDetails = paymentReportCalculator.calculateTotalOwed(
                report,
                it
            )

            val reportMongo = PaymentReportMongo.Companion.fromInputModel(report, paymentListDetails)

            val existingReport = paymentMongoRepository.findById(report.id!!).orElse(null)
                ?: return@run failure(
                    ApiError.NotFound(
                        "Payment Report not found",
                        "No report found with the provided ID."
                    )
                )

            if (existingReport.sealed) {
                return@run failure(
                    ApiError.InvalidField(
                        "Payment Report is sealed",
                        "Cannot update a sealed report."
                    )
                )
            }

            val updatedReport = paymentMongoRepository.save(reportMongo)
            return@run success(updatedReport)
        }
    }

    fun sealPaymentReport(id: String): Either<ApiError, PaymentReportMongo> {
        return transactionManager.run {
            val report = paymentMongoRepository.findById(id).orElse(null)
                ?: return@run failure(
                    ApiError.NotFound(
                        "Payment Report not found",
                        "No report found with the provided ID."
                    )
                )

            if (report.sealed) {
                return@run failure(
                    ApiError.InvalidField(
                        "Payment Report already sealed",
                        "The report with ID $id is already sealed."
                    )
                )
            }

            val success = paymentMongoRepository.seal(id,false) // assumes returns Boolean

            if (!success) {
                return@run failure(
                    ApiError.InternalServerError(
                        "Failed to seal payment report",
                        "An error occurred while trying to seal the report with ID $id."
                    )
                )
            }

            val updated = paymentMongoRepository.findById(id).orElse(null)
                ?: return@run failure(
                    ApiError.InternalServerError(
                        "Failed to create payment report representative",
                        "An internal error occurred while trying to create the report representative for the sealed report with ID $id."
                    )
                )

            success(updated)
        }
    }

    fun getPaymentReportByCompetition(competitionId: Int): Either<ApiError, List<PaymentReportMongo>> {
        val payments = paymentMongoRepository.findByCompetitionId(competitionId, false).filterIsInstance<PaymentReportMongo>()
        return success(payments)
    }

}