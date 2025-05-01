package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.UserRole
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.domain.UsersDomain
import pt.arbitros.arbnet.http.model.UserInputModel
import pt.arbitros.arbnet.http.model.UserUpdateInputModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo
import java.time.LocalDate

@Component
class UsersService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
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
        user: UserInputModel,
    ): Int =
        transactionManager.run {
            val usersRepository = it.usersRepository

            validateUser(
                user.name,
                user.phoneNumber,
                user.address,
                user.email,
                user.password,
                user.birthDate,
                user.iban,
            )

            existsByParams(user.email, user.iban, user.phoneNumber)

            val id =
                usersRepository.createUser(
                    user.name,
                    user.phoneNumber,
                    user.address,
                    user.email,
                    user.password,
                    LocalDate.parse(user.birthDate),
                    user.iban,
                )
            id
        }

    fun updateUser(
        user : UserUpdateInputModel,
    ): Boolean =
        transactionManager.run {
            val usersRepository = it.usersRepository

            validateUser(
                user.name,
                user.phoneNumber,
                user.address,
                user.email,
                user.password,
                user.birthDate,
                user.iban,
            )

            usersRepository.getUserById(user.id) ?: throw Exception("User with id ${user.id} not found")
            existsByParams(user.email, user.iban, user.phoneNumber)
            val updated =
                usersRepository.updateUser(
                    user.id,
                    user.name,
                    user.phoneNumber,
                    user.address,
                    user.email,
                    user.password,
                    LocalDate.parse(user.birthDate),
                    user.iban,
                )
            updated
        }

    fun deleteUser(id: Int) =
        transactionManager.run {
            val usersRepository = it.usersRepository
            usersRepository.getUserById(id) ?: throw Exception("User with id $id not found")
            val deleted = usersRepository.deleteUser(id)
            deleted
        }

    fun existsByParams(email: String, iban: String, phoneNumber: String) =
        transactionManager.run {
            val usersRepository = it.usersRepository
            if (usersRepository.existsByEmail(email)) {
                throw Exception("User with email $email already exists")
            }
            if (usersRepository.existsByPhoneNumber(phoneNumber)) {
                throw Exception("User with phone number $phoneNumber already exists")
            }
            if (usersRepository.existsByIban(iban)) {
                throw Exception("User with iban $iban already exists")
            }
        }

    fun updateRoles(
        id: Int,
        role: String,
        addOrRemove: Boolean,
    ): Boolean = transactionManager.run {
        val usersRepository = it.usersRepository
        val adminRepository = it.adminRepository
        val arbitrationCouncilRepository = it.arbitrationCouncilRepository
        val refereeRepository = it.refereeRepository

        if (!usersDomain.validRole(role)) throw Exception("Role $role is not valid")
        val user = usersRepository.getUserById(id) ?: throw Exception("User with id $id not found")

        if (addOrRemove && user.roles.contains(role)) {
            throw Exception("User with id $id already has role $role")
        }
        if (!addOrRemove && !user.roles.contains(role)) {
            throw Exception("User with id $id does not have role $role")
        }

        val roleActions = mapOf(
            UserRole.ADMIN.roleName to Pair(
                { adminRepository.createAdmin(id) },
                { adminRepository.deleteAdmin(id) }
            ),
            UserRole.ARBITRATION_COUNCIL.roleName to Pair(
                { arbitrationCouncilRepository.createCouncilMember(id) },
                { arbitrationCouncilRepository.deleteCouncilMember(id) }
            ),
            UserRole.REFEREE.roleName to Pair(
                { refereeRepository.createReferee(id) },
                { refereeRepository.deleteReferee(id) }
            )
        )

        val (addAction, removeAction) = roleActions[role]
            ?: throw Exception("Role $role is not supported for update")

        val newRoles = if (addOrRemove) {
            addAction()
            user.roles + role
        } else {
            removeAction()
            user.roles - role
        }

        usersRepository.updateRoles(id, newRoles)
    }

    fun validateUser(
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        password: String,
        birthDate: String,
        iban: String,
    ) {
        require(usersDomain.validName(name)) { "Invalid name" }
        require(usersDomain.validPhoneNumber(phoneNumber)) { "Invalid phone number" }
        require(usersDomain.validAddress(address)) { "Invalid address" }
        require(usersDomain.validEmail(email)) { "Invalid email" }
        require(usersDomain.validPassword(password)) { "Invalid password" }
        require(usersDomain.validBirthDate(birthDate)) { "Invalid birth date" }
        require(usersDomain.validatePortugueseIban(iban)) { "Invalid IBAN" }
    }

}


