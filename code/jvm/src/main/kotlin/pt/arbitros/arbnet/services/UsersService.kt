package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.Role
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.domain.UsersDomain
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.model.UserInputModel
import pt.arbitros.arbnet.http.model.UserUpdateInputModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo
import java.time.LocalDate

sealed class UsersError {
    data object RoleNotFound : UsersError()

    data object UserNotFound : UsersError()

    data object UserWithoutRole : UsersError()

    data object UserAlreadyHasRole : UsersError()

    data object EmailAlreadyUsed : UsersError()

    data object PhoneNumberAlreadyUsed : UsersError()

    data object IbanAlreadyUsed : UsersError()

    data object EmailNotFound : UsersError()
}

@Component
class UsersService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val utilsDomain: UtilsDomain
    // private val clock: Clock
) {
    fun getUserById(id: Int): Either<UsersError, Users> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val user = usersRepository.getUserById(id) ?: return@run failure(UsersError.UserNotFound)
            return@run success(user)
        }

    fun getUserByEmail(email: String): Either<UsersError, Users> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val user = usersRepository.getUserByEmail(email) ?: return@run failure(UsersError.EmailNotFound)
            return@run success(user)
        }

    fun createUser(user: UserInputModel): Either<UsersError, Int> =
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

            checkIfExistsInRepo(user.email, user.iban, user.phoneNumber)

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
            return@run success(id)
        }

    fun updateUser(user: UserUpdateInputModel): Either<UsersError, Boolean> =
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
            val userInfo = usersRepository.getUserById(user.id) ?: return@run failure(UsersError.UserNotFound)
            if (userInfo as UserUpdateInputModel != user) {
                checkIfExistsInRepo(user.email, user.iban, user.phoneNumber, userInfo.id)
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
                return@run success(updated)
            }
            return@run success(true)
        }

    fun deleteUser(id: Int): Either<UsersError, Boolean> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            usersRepository.getUserById(id) ?: return@run failure(UsersError.UserNotFound)
            val deleted = usersRepository.deleteUser(id)
            return@run success(deleted)
        }

    fun checkIfExistsInRepo(
        email: String,
        iban: String,
        phoneNumber: String,
        excludeUserId: Int? = null,
    ): Either<UsersError, Boolean> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            if (excludeUserId == null) {
                if (usersRepository.existsByEmail(email)) return@run failure(UsersError.EmailAlreadyUsed)
                if (usersRepository.existsByPhoneNumber(phoneNumber)) return@run failure(UsersError.PhoneNumberAlreadyUsed)
                if (usersRepository.existsByIban(iban)) return@run failure(UsersError.IbanAlreadyUsed)
            } else {
                if (usersRepository.existsByEmailExcludingId(email, excludeUserId)) return@run failure(UsersError.EmailAlreadyUsed)
                if (usersRepository.existsByPhoneNumberExcludingId(
                        phoneNumber,
                        excludeUserId,
                    )
                ) {
                    return@run failure(UsersError.PhoneNumberAlreadyUsed)
                }
                if (usersRepository.existsByIbanExcludingId(iban, excludeUserId)) return@run failure(UsersError.IbanAlreadyUsed)
            }

            return@run success(true)
        }

    fun updateUserRoles(
        userId: Int,
        roleId: Int,
        addOrRemove: Boolean,
    ): Either<UsersError, Boolean> =
        // Boolean =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val roleRepository = it.roleRepository
            val usersRolesRepository = it.usersRolesRepository

            roleRepository.getRoleName(roleId) ?: return@run failure(UsersError.RoleNotFound)
            usersRepository.getUserById(userId) ?: return@run failure(UsersError.UserNotFound)

            val hasRole = usersRolesRepository.userHasRole(userId, roleId)
            val success : Boolean =
                when {
                    addOrRemove && !hasRole -> usersRolesRepository.addRoleToUser(userId, roleId)
                    !addOrRemove && hasRole -> usersRolesRepository.removeRoleFromUser(userId, roleId)
                    !hasRole -> return@run failure(UsersError.UserWithoutRole)
                    else -> return@run failure(UsersError.UserAlreadyHasRole)
                }

            return@run success(success)
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
        require(utilsDomain.validName(name)) { "Invalid name" }
        require(utilsDomain.validPhoneNumber(phoneNumber)) { "Invalid phone number" }
        require(utilsDomain.validAddress(address)) { "Invalid address" }
        require(utilsDomain.validEmail(email)) { "Invalid email" }
        require(usersDomain.validPassword(password)) { "Invalid password" }
        require(usersDomain.validBirthDate(birthDate)) { "Invalid birth date" }
        require(usersDomain.validatePortugueseIban(iban)) { "Invalid IBAN" }
    }

    fun getAllRoles(): Either<UsersError, List<Role> >{
        TODO("Not yet implemented")
        //check if it makes sense to create a separate service for this
    }
}
