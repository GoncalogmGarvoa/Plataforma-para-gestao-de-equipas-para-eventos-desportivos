package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.services.payment.PaymentValue

interface PaymentValuesRepository {

    fun getPaymentValueByName(name: String): Double?

    fun getPaymentValues(): List<PaymentValue>
}