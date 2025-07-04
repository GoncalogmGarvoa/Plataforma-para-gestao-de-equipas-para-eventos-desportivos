package pt.arbitros.arbnet.services.payment

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.PaymentDetails
import pt.arbitros.arbnet.domain.PaymentValuesPerReferee
import pt.arbitros.arbnet.http.model.payment_report.PaymentReportInputModel
import pt.arbitros.arbnet.repository.Transaction
import java.time.DayOfWeek


enum class PaymentValueType(val dbName: String) {
    PRESENCE("presence"),
    WEEKDAY("weekday"),
    TRANSPORTATION("transportation"),
    MEALS("meals"),
    JURY_REFEREE("jury-ref");
}

@Component
object PaymentReportCalculator {

    fun calculateTotalOwed(
        paymentReport : PaymentReportInputModel,
        transaction : Transaction
    ): List<PaymentDetails> {

        val paymentDetailsList = mutableListOf<PaymentDetails>()

        val paymentValues = transaction.paymentValuesRepository.getPaymentValues()

        for (info in paymentReport.paymentInfoPerReferee) {

            var presences = 0
            var weekdays = 0

            for (session in info.sessionsPresence) {
                if (session.morning) {
                    presences++
                }
                if (session.afternoon) {
                    presences++
                }

                val dayOfWeek = transaction.matchDayRepository.getMatchDayById(session.matchDay)!!.matchDate.dayOfWeek

                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                    weekdays++
                }
            }

            val presenceValue = paymentValues.find { it.name == PaymentValueType.PRESENCE.dbName }!!.value * presences
            val weekDayValue = paymentValues.find { it.name == PaymentValueType.WEEKDAY.dbName }!!.value * weekdays
            val transportationValue = paymentValues.find { it.name == PaymentValueType.TRANSPORTATION.dbName }!!.value
            val juryRefereeValue = if (paymentReport.juryRefere == info.name) {
                paymentValues.find { it.name == PaymentValueType.JURY_REFEREE.dbName }!!.value
            } else {
                0.0
            }
            val mealsValue = paymentValues.find { it.name == PaymentValueType.MEALS.dbName }!!.value * info.numberOfMeals
            val totalOwed = presenceValue + weekDayValue + transportationValue + juryRefereeValue + mealsValue
            val owedLeft = totalOwed - info.payedAmount

            val paymentValuesPerReferee = PaymentValuesPerReferee(
                presence = presenceValue,
                weekDay = weekDayValue,
                transportation = transportationValue,
                meals = mealsValue,
                totalOwed = totalOwed,
                owedLeft = owedLeft
            )

            paymentDetailsList.add(
                PaymentDetails(
                    paymentRefInfo = info,
                    paymentValues = paymentValuesPerReferee
                )
            )
        }

        return paymentDetailsList

    }

}

data class PaymentValue(
    val name : String,
    val value : Double
)