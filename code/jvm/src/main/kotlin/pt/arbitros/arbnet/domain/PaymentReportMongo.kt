package pt.arbitros.arbnet.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pt.arbitros.arbnet.http.model.payment_report.PaymentReportInputModel

@Document(collection = "payment_reports")
data class PaymentReportMongo(
    @Id val id : String? = null,
    val authorName : String,
    val reportType : String = "PAY_REPORT",
    val competitionId : Int,
    val sealed: Boolean = false,
    val juryRefere: String, //na folha de pagamento é necessário indicar o nome do JÁ
    val paymentCoverSheet: PaymentCoverSheet,
    val paymentInfoPerReferee: List<PaymentDetails>
){
    companion object {
        fun fromInputModel(input: PaymentReportInputModel, list : List <PaymentDetails>  ): PaymentReportMongo {
            return PaymentReportMongo(
                id = input.id, // pode ser null (criação) ou presente (‘update’)
                authorName = input.authorName,
                competitionId = input.competitionId,
                sealed = input.sealed,
                juryRefere = input.juryRefere,
                paymentCoverSheet = input.paymentCoverSheet,
                paymentInfoPerReferee = list
            )
        }
    }
}

data class PaymentDetails(
    val paymentRefInfo : PaymentInfoPerReferee,
    val paymentValues : PaymentValuesPerReferee
)

data class PaymentInfoPerReferee (
    val name: String,
    val nib: String,
    val sessionsPresence: List<SessionsPresence>,
    val numberOfMeals : Int,
    val payedAmount : Double
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

data class PaymentValuesPerReferee (
    val presence: Double,
    val weekDay: Double,
    val transportation: Double,
    val meals: Double,
    val totalOwed: Double,
    val owedLeft: Double,
)
