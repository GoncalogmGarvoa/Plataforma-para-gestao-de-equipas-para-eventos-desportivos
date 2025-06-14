package pt.arbitros.arbnet.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pt.arbitros.arbnet.http.model.payment_report.PaymentReportInputModel

@Document(collection = "payment_reports")
data class PaymentReportMongo(
    @Id val id : String? = null,
    val reportType : String,
    val competitionId : Int,
    val sealed: Boolean = false,
    val juryRefere: String,
    val paymentCoverSheet: PaymentCoverSheet,
    val paymentPerReferee: List<PaymentPerReferee>
){
    companion object {
        fun fromInputModel(input: PaymentReportInputModel): PaymentReportMongo {
            return PaymentReportMongo(
                id = input.id, // pode ser null (criação) ou presente (‘update’)
                reportType = input.reportType,
                competitionId = input.competitionId,
                sealed = input.sealed,
                juryRefere = input.juryRefere,
                paymentCoverSheet = input.paymentCoverSheet,
                paymentPerReferee = input.paymentPerReferee
            )
        }
    }
}


data class PaymentPerReferee (
    val name: String,
    val nib: String,
    val sessionsPresence: List<SessionsPresence>
)

data class SessionsPresence (
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