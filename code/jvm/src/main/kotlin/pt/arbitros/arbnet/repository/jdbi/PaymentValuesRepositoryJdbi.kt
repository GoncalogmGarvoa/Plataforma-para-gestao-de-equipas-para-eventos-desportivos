package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.repository.PaymentValuesRepository
import pt.arbitros.arbnet.services.payment.validation.PaymentValue

class PaymentValuesRepositoryJdbi(private val handle: Handle) : PaymentValuesRepository {
    override fun getPaymentValueByName(name: String): Double? {
        return handle
            .createQuery(
                """select value from dbp.payment_values where name = :name""",
            )
            .bind("name", name)
            .mapTo(Double::class.java)
            .findOne()
            .orElse(null)
    }

    override fun getPaymentValues(): List<PaymentValue> {
        return handle
            .createQuery(
                """select name, value from dbp.payment_values""",
            )
            .mapTo<PaymentValue>()
            .list()

    }

}