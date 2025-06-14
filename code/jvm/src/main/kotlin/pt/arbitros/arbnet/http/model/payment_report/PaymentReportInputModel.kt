package pt.arbitros.arbnet.http.model.payment_report

data class PaymentReportInputModel (
    val id : String? = null,
    val reportType : String,
    val competitionId : Int,
    val sealed: Boolean = false,
    val juryRefere: String,
    val paymentCoverSheet: PaymentCoverSheet,
    val paymentPerReferee: List<PaymentPerReferee>
)

class PaymentPerReferee (
    val name: String,
    val nib: String,
    val sessionsPresence: List<SessionsPresence>
)

class SessionsPresence (
    val matchDay: Int,
    val morning: Boolean,
    val morningTime: String,
    val afternoon: Boolean,
    val afternoonTime: String,
)

data class PaymentCoverSheet (
    val style: String,
    val councilName: String,
    val eventName: String,
    val venue: String,
    val eventDate: String,
    val eventTime: String,
    val location: String,
    val organization: String,
)
