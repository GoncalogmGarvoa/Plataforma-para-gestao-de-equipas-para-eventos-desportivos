package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.domain.UsersDomain
import pt.arbitros.arbnet.repository.TransactionManager
import java.time.LocalDate

@Component
class UsersService(
    @Qualifier("jdbiTransactionManager") private val transactionManager: TransactionManager,
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

    fun updateRoles(
        id: Int,
        roles: String,
        addOrRemove: Boolean,
    ): Boolean =
        transactionManager.run {
            val usersRepository = it.usersRepository
            if (!usersDomain.validRole(roles)) throw Exception("Role $roles is not valid")
            val user = usersRepository.getUserById(id) ?: throw Exception("User with id $id not found")

            if (addOrRemove && user.roles.contains(roles)) {
                throw Exception("User with id $id already has role $roles")
            }
            if (!addOrRemove && !user.roles.contains(roles)) {
                throw Exception("User with id $id does not have role $roles")
            }

            val newRoles =
                if (addOrRemove) {
                    user.roles + roles
                } else {
                    user.roles - roles
                }

            val updated = usersRepository.updateRoles(id, newRoles)
            updated
        }
}
