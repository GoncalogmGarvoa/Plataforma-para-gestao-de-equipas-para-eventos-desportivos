package pt.arbitros.arbnet.services.payment.validation

import pt.arbitros.arbnet.domain.PaymentReportMongo
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.repository.Transaction
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.success

object PaymentReportValidator {

    fun validate(
        paymentReport: PaymentReportMongo,
        createOrUpdate: Boolean,
        jdbiTransaction: Transaction
    ): Either<ApiError, Unit>{

        val competitionRepository = jdbiTransaction.competitionRepository

        if (competitionRepository.getCompetitionById(paymentReport.competitionId) == null) {
            return failure(
                ApiError.InvalidField(
                    "Invalid competition ID",
                    "The provided competition ID does not exist or is invalid."
                )
            )
        }

        if (createOrUpdate && paymentReport.id != null)
            return failure(
                ApiError.InvalidField(
                    "PaymentReport ID should not be provided for creation",
                    "The report ID must be null when creating a new PaymentReport."
                )
            )

        if (paymentReport.sealed)
            return failure(
                ApiError.InvalidField(
                    "PaymentReport is sealed",
                    "Sealed paymentreports cannot be modified, and new PaymentReport cant be sealed."
                )
            )

        if (!createOrUpdate && paymentReport.id == null)
            return failure(
                ApiError.InvalidField(
                    "PaymentReport ID is required for update",
                    "The PaymentReport ID must be provided when updating an existing PaymentReport."
                )
            )



        return success(Unit)
    }
}