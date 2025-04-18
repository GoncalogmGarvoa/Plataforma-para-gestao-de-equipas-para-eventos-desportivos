package pt.arbitros.arbnet.services

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.repository.TransactionManager


@Component
class UsersService(
    private val transactionManager: TransactionManager,
    //private val usersDomain: User,
    //private val clock: Clock
) {
    fun getUserById(id: Int): Users {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val username = usersRepository.getUserById(id) ?: throw Exception("User with id $id not found")
            username
        }
    }


}
