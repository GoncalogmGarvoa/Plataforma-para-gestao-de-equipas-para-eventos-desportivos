package pt.arbitros.arbnet.repository.mem

import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.repository.Transaction
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.repository.jdbi.TransactionJdbi

@Component
class TransactionManagerMem(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = TransactionJdbi(handle)
            block(transaction)
        }
}
