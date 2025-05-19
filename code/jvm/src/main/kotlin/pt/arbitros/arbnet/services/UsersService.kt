package pt.arbitros.arbnet.services

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.domain.universal.Role
import pt.arbitros.arbnet.domain.users.Token
import pt.arbitros.arbnet.domain.users.Users
import pt.arbitros.arbnet.domain.users.UsersDomain
import pt.arbitros.arbnet.domain.users.UsersUtils
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

    data object InvalidName : UsersError()

    data object InvalidAddress : UsersError()

    data object InvalidPassword : UsersError()

    data object InvalidBirthDate : UsersError()

    data object InvalidIban : UsersError()

    data object InvalidPhoneNumber : UsersError()

    data object InvalidEmail : UsersError()

    data object NeededFullName : UsersError()

    data object MissingField : UsersError() // todo

    data object UserOrPasswordAreInvalid : UsersError() // todo
}

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError() // todo
}

data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant,
)

@Component
class UsersService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val usersUtils: UsersUtils,
    private val usersDomain: UsersDomain,
    private val utilsDomain: UtilsDomain,
    private val clock: Clock,
) {
    fun createToken(
        email: String,
        password: String,
    ): Either<UsersError, TokenExternalInfo> =
        transactionManager.run {
            if (email.isBlank() || password.isBlank()) {
                return@run failure(UsersError.MissingField)
            }
            val usersRepository = it.usersRepository
            val user: Users = usersRepository.getUserByEmail(email) ?: return@run failure(UsersError.EmailNotFound)

            if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                    return@run failure(UsersError.UserOrPasswordAreInvalid)
                }
            }
            val tokenValue = usersDomain.generateTokenValue()
            val now = clock.now()
            val newToken =
                Token(
                    usersDomain.createTokenValidationInformation(tokenValue),
                    user.id,
                    createdAt = now,
                    lastUsedAt = now,
                )
            usersRepository.createToken(newToken, usersDomain.maxNumberOfTokensPerUser)
            return@run success(
                TokenExternalInfo(
                    tokenValue,
                    usersDomain.getTokenExpiration(newToken),
                ),
            )
        }

    fun revokeToken(token: String): Either<UsersError, Boolean> {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        return transactionManager.run {
            it.usersRepository.removeTokenByValidationInfo(tokenValidationInfo)
            return@run success(true)
        }
    }

    fun getUserByToken(token: String): Either<UsersError, Users>? {
        if (!usersDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && usersDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                return@run success(userAndToken.first)
            } else {
                return@run failure(UsersError.UserNotFound)
            }
        }
    }

    fun getUserById(id: Int): Either<UsersError, Pair<Users, List<String>>> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val usersRolesRepository = it.usersRolesRepository
            val rolesRepository = it.roleRepository
            val user = usersRepository.getUserById(id) ?: return@run failure(UsersError.UserNotFound)
            val rolesId = usersRolesRepository.getUserRolesId(id)
            val roles = rolesId.mapNotNull { elem -> rolesRepository.getRoleName(elem) }
            return@run success(user to roles)
        }

    fun getUserByEmail(email: String): Either<UsersError, Pair<Users, List<String>>> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val usersRolesRepository = it.usersRolesRepository
            val rolesRepository = it.roleRepository
            val user = usersRepository.getUserByEmail(email) ?: return@run failure(UsersError.EmailNotFound)
            val rolesId = usersRolesRepository.getUserRolesId(user.id)
            val roles = rolesId.mapNotNull { elem -> rolesRepository.getRoleName(elem) }
            return@run success(user to roles)
        }

    fun createUser(user: UserInputModel): Either<UsersError, Int> =
        transactionManager.run {
            val usersRepository = it.usersRepository

            val validateResult =
                validateUser(
                    user.name,
                    user.phoneNumber,
                    user.address,
                    user.email,
                    user.password,
                    user.birthDate,
                    user.iban,
                )

            if (validateResult is Failure) {
                return@run failure(validateResult.value)
            }

            val checkRepoResult = checkIfExistsInRepo(user.email, user.iban, user.phoneNumber)
            if (checkRepoResult is Failure) {
                return@run failure(checkRepoResult.value)
            }
            val passwordValidationInfo = usersDomain.createPasswordValidationInformation(user.password)

            val id =
                usersRepository.createUser(
                    user.name,
                    user.phoneNumber,
                    user.address,
                    user.email,
                    passwordValidationInfo,
                    LocalDate.parse(user.birthDate),
                    user.iban,
                )
            return@run success(id)
        }

    fun updateUser(user: UserUpdateInputModel): Either<UsersError, Boolean> =
        transactionManager.run {
            val usersRepository = it.usersRepository

            val validateResult =
                validateUser(
                    user.name,
                    user.phoneNumber,
                    user.address,
                    user.email,
                    user.password,
                    user.birthDate,
                    user.iban,
                )

            if (validateResult is Failure) {
                return@run validateResult
            }

            usersRepository.getUserById(user.id) ?: return@run failure(UsersError.UserNotFound)
            val passwordValidationInfo = usersDomain.createPasswordValidationInformation(user.password)

            checkIfExistsInRepo(user.email, user.iban, user.phoneNumber, user.id)
            val updated =
                usersRepository.updateUser(
                    user.id,
                    user.name,
                    user.phoneNumber,
                    user.address,
                    user.email,
                    passwordValidationInfo,
                    LocalDate.parse(user.birthDate),
                    user.iban,
                )
            return@run success(updated)
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
            val success: Boolean =
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
    ): Either<UsersError, Unit> {
        if (!name.contains(" ")) return failure(UsersError.NeededFullName)
        if (!utilsDomain.validName(name)) return failure(UsersError.InvalidName)
        if (!utilsDomain.validPhoneNumber(phoneNumber)) return failure(UsersError.InvalidPhoneNumber)
        if (!utilsDomain.validAddress(address)) return failure(UsersError.InvalidAddress)
        if (!utilsDomain.validEmail(email)) return failure(UsersError.InvalidEmail)
        // if (!usersDomain.validPassword(password)) return failure(UsersError.InvalidPassword)
        if (!usersUtils.validBirthDate(birthDate)) return failure(UsersError.InvalidBirthDate)
        if (!usersUtils.validatePortugueseIban(iban)) return failure(UsersError.InvalidIban)

        return success(Unit)
    }

    // TODO check if it makes sense to create a separate service for this
    fun getAllRoles(): Either<UsersError, List<Role>> =
        transactionManager.run {
            val roleRepository = it.roleRepository
            val roles = roleRepository.getAllRoles()
            if (roles.isEmpty()) return@run failure(UsersError.RoleNotFound) // TODO check if this is the right error
            return@run success(roles)
        }
}
