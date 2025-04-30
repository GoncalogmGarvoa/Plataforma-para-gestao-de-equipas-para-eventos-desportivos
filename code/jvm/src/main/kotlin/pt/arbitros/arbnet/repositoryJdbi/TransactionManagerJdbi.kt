package pt.arbitros.arbnet.repositoryJdbi

import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.repository.Transaction
import pt.arbitros.arbnet.repository.TransactionManager

@Component
class TransactionManagerJdbi(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = TransactionJdbi(handle)
            block(transaction)
        }
}
