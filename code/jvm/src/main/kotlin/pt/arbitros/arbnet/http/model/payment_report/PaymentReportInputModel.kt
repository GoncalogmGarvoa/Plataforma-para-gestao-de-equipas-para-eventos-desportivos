package pt.arbitros.arbnet.http.model.payment_report

import pt.arbitros.arbnet.domain.PaymentCoverSheet
import pt.arbitros.arbnet.domain.PaymentPerReferee

data class PaymentReportInputModel (
    val id : String? = null,
    val competitionId : Int,
    val sealed: Boolean = false,
    val juryRefere: String,
    val paymentCoverSheet: PaymentCoverSheet,
    val paymentPerReferee: List<PaymentPerReferee>
)


