package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.PaymentReportMongo
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.payment_report.PaymentReportInputModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.repository.mongo.PaymentReportMongoRepository
import pt.arbitros.arbnet.transactionRepo

@Component
class PaymentReportService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val paymentMongoRepository: PaymentReportMongoRepository,
    private val utilsDomain: UtilsDomain,
) {

    fun createPaymentReport(report: PaymentReportInputModel): Either<ApiError, PaymentReportMongo> {
        return transactionManager.run {
            val competitionRepository = it.competitionRepository //todo use this to validate competitionId

            val reportMongo = PaymentReportMongo.fromInputModel(report)

            val result = paymentMongoRepository.save(reportMongo)

            return@run success(result)
        }
    }

    fun getAllPaymentReports(): Either<ApiError, List<PaymentReportMongo>> {
        return success(paymentMongoRepository.findAll())
    }

    fun getPaymentReportById(id: String): Either<ApiError, PaymentReportMongo?> {
        val result = paymentMongoRepository.findById(id)
        return if (result.isPresent) success(result.get())
        else failure(ApiError.NotFound(
            "Payment Report not found",
            "No report found with the provided ID."
        ))
    }

    fun updatePaymentReport(report: PaymentReportInputModel): Either<ApiError, PaymentReportMongo> {
        return transactionManager.run {

            val competitionRepository = it.competitionRepository
            val reportMongo = PaymentReportMongo.fromInputModel(report)

            val existingReport = paymentMongoRepository.findById(report.id!!).orElse(null)
                ?: return@run failure(ApiError.NotFound(
                    "Payment Report not found",
                    "No report found with the provided ID."
                ))

            if (existingReport.sealed) {
                return@run failure(ApiError.InvalidField(
                    "Payment Report is sealed",
                    "Cannot update a sealed report."
                ))
            }

            val updatedReport = paymentMongoRepository.save(reportMongo)
            return@run success(updatedReport)
        }
    }

    fun sealPaymentReport(id: String): Either<ApiError, PaymentReportMongo> {
        return transactionManager.run {
            val report = paymentMongoRepository.findById(id).orElse(null)
                ?: return@run failure(ApiError.NotFound(
                    "Payment Report not found",
                    "No report found with the provided ID."
                ))

            if (report.sealed) {
                return@run failure(ApiError.InvalidField(
                    "Payment Report already sealed",
                    "The report with ID $id is already sealed."
                ))
            }

            val success = paymentMongoRepository.seal(id,false) // assumes returns Boolean

            if (!success) {
                return@run failure(ApiError.InternalServerError(
                    "Failed to seal payment report",
                    "An error occurred while trying to seal the report with ID $id."
                ))
            }

            it.reportRepository.createReport(
                id,
                report.reportType,
                report.competitionId
            )

            val updated = paymentMongoRepository.findById(id).orElse(null)
                ?: return@run failure(ApiError.InternalServerError(
                    "Failed to create payment report representative",
                    "An internal error occurred while trying to create the report representative for the sealed report with ID $id."
                ))

            success(updated)
        }
    }

}