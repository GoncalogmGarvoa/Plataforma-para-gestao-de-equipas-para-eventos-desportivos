package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.Transaction
import pt.arbitros.arbnet.repository.UsersRepository

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
