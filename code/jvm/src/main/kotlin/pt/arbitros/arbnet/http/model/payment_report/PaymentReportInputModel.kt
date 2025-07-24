package pt.arbitros.arbnet.http.model.payment_report

import pt.arbitros.arbnet.domain.PaymentCoverSheet
import pt.arbitros.arbnet.domain.PaymentInfoPerReferee

data class PaymentReportInputModel (
    val id : String? = null,
    val authorName : String,
    val competitionId : Int,
    val sealed: Boolean = false,
    val juryRefere: String,
    val paymentCoverSheet: PaymentCoverSheet,
    val paymentInfoPerReferee: List<PaymentInfoPerReferee>
)


