package pt.arbitros.arbnet.services

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.domain.UsersDomain
import pt.arbitros.arbnet.repository.TransactionManager
import java.time.LocalDate

@Component
class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    // private val clock: Clock
) {
    fun getUserById(id: Int): Users =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val user = usersRepository.getUserById(id) ?: throw Exception("User with id $id not found")
            user
        }

    fun getUserByEmail(email: String): Users =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val user = usersRepository.getUserByEmail(email) ?: throw Exception("User with email $email not found")
            user
        }

    fun createUser(
        name: String,
        phoneNumber: Int,
        address: String,
        email: String,
        password: String,
        birthDate: String,
        iban: String,
    ): Int =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val id =
                usersRepository.createUser(
                    name,
                    phoneNumber,
                    address,
                    email,
                    password,
                    LocalDate.parse(birthDate),
                    iban,
                )
            id
        }

    fun updateUser(
        name: String,
        phoneNumber: Int,
        address: String,
        email: String,
        password: String,
        birthDate: String,
        iban: String,
    ): Boolean =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val updated =
                usersRepository.updateUser(
                    name,
                    phoneNumber,
                    address,
                    email,
                    password,
                    LocalDate.parse(birthDate),
                    iban,
                )
            updated
        }

    fun deleteUser(id: Int): Boolean =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val deleted = usersRepository.deleteUser(id)
            deleted
        }

    fun existsByEmail(email: String): Boolean =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val exists = usersRepository.existsByEmail(email)
            exists
        }
}
