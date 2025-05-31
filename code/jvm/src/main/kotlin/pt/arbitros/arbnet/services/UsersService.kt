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
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.UserInputModel
import pt.arbitros.arbnet.http.model.UserUpdateInputModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo
import java.time.LocalDate

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
    private val userNotFoundId = ApiError.NotFound(
        "User not found",
        "No user found with the provided ID",
    )


    fun createToken(
        email: String,
        password: String,
    ): Either<ApiError, TokenExternalInfo> =
        transactionManager.run {
            if (email.isBlank() || password.isBlank()) {
                return@run failure(ApiError.MissingField("Email and password are required", "Either email or password is missing or both"))
            }
            val usersRepository = it.usersRepository
            val user: Users = usersRepository.getUserByEmail(email) ?:
            return@run failure(ApiError.NotFound("User not found", "No user found with the provided email"))

            if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                return@run failure(ApiError.InvalidField("Invalid password", "The provided password does not match the user's password"))
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

    fun revokeToken(token: String): Either<ApiError, Boolean> {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        return transactionManager.run {
            it.usersRepository.removeTokenByValidationInfo(tokenValidationInfo)
            return@run success(true)
        }
    }

    fun getUserByToken(token: String): Either<ApiError, Users> {
        if (!usersDomain.canBeToken(token)) {
            return failure(ApiError.InvalidField(
                "Invalid token",
                "The provided token is not valid or does not match the expected format.",
            ))
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && usersDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                return@run success(userAndToken.first)
            } else {
                return@run failure(ApiError.NotFound(
                    "User not found",
                    "No user found with the provided token or the token is expired",
                ))
            }
        }
    }

    fun getUserById(id: Int): Either<ApiError, Pair<Users, List<String>>> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val usersRolesRepository = it.usersRolesRepository
            val rolesRepository = it.roleRepository
            val user = usersRepository.getUserById(id) ?: return@run failure(userNotFoundId)
            val rolesId = usersRolesRepository.getUserRolesId(id)
            val roles = rolesId.mapNotNull { elem -> rolesRepository.getRoleName(elem) }
            return@run success(user to roles)
        }

    fun getUserByEmail(email: String): Either<ApiError, Pair<Users, List<String>>> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val usersRolesRepository = it.usersRolesRepository
            val rolesRepository = it.roleRepository
            val user = usersRepository.getUserByEmail(email) ?: return@run failure(ApiError.NotFound(
                "User not found",
                "No user found with the provided email",
            ))
            val rolesId = usersRolesRepository.getUserRolesId(user.id)
            val roles = rolesId.mapNotNull { elem -> rolesRepository.getRoleName(elem) }
            return@run success(user to roles)
        }

    fun createUser(user: UserInputModel): Either<ApiError, Int> =
        transactionManager.run {
            val usersRepository = it.usersRepository

            val validateResult =
                validateUser(
                    user.name,
                    user.phoneNumber,
                    user.address,
                    user.email,
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
            val passwordValidationInfo =
                try {
                    usersDomain.createPasswordValidationInformation(user.password)
                } catch (e: Exception) {
                    return@run failure(ApiError.InvalidField(
                        "Invalid password",
                        "The provided password is incorrect",
                    )
                    )
                }

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

    fun updateUser(user: UserUpdateInputModel): Either<ApiError, Boolean> =
        transactionManager.run {
            val usersRepository = it.usersRepository

            //TODO validate new password if provided

            val validateResult =
                validateUser(
                    user.name,
                    user.phoneNumber,
                    user.address,
                    user.email,
                    user.birthDate,
                    user.iban,
                )

            if (validateResult is Failure) {
                return@run validateResult
            }

            usersRepository.getUserById(user.id) ?: return@run failure(userNotFoundId)
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

    fun deleteUser(id: Int): Either<ApiError, Boolean> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            usersRepository.getUserById(id) ?: return@run failure(userNotFoundId)
            val deleted = usersRepository.deleteUser(id)
            return@run success(deleted)
        }

    fun checkIfExistsInRepo(
        email: String,
        iban: String,
        phoneNumber: String,
        excludeUserId: Int? = null,
    ): Either<ApiError, Boolean> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            if (excludeUserId == null) {
                if (usersRepository.existsByEmail(email)) return@run failure(inUseError("email"))
                if (usersRepository.existsByPhoneNumber(phoneNumber)) return@run failure(inUseError("phone number"))
                if (usersRepository.existsByIban(iban)) return@run failure(inUseError("IBAN"))
            } else {
                if (usersRepository.existsByEmailExcludingId(email, excludeUserId)) return@run failure( inUseError("email"))
                if (usersRepository.existsByPhoneNumberExcludingId(phoneNumber, excludeUserId)) { return@run failure(inUseError("phone number")) }
                if (usersRepository.existsByIbanExcludingId(iban, excludeUserId)) return@run failure(inUseError("IBAN"))
            }

            return@run success(true)
        }

    fun updateUserRoles(
        userId: Int,
        roleId: Int,
        addOrRemove: Boolean,
    ): Either<ApiError, Boolean> =
        // Boolean =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val roleRepository = it.roleRepository
            val usersRolesRepository = it.usersRolesRepository

            roleRepository.getRoleName(roleId) ?: return@run failure(ApiError.NotFound("Role not found", "The provided role does not exist"))
            usersRepository.getUserById(userId) ?: return@run failure(ApiError.NotFound("User not found", "The provided user does not exist"))

            val hasRole = usersRolesRepository.userHasRole(userId, roleId)
            val success: Boolean =
                when {
                    addOrRemove && !hasRole -> usersRolesRepository.addRoleToUser(userId, roleId)
                    !addOrRemove && hasRole -> usersRolesRepository.removeRoleFromUser(userId, roleId)
                    !hasRole -> return@run failure(ApiError.InvalidField(
                        "User does not have the role",
                        "The user does not have the specified role to remove",
                    ))
                    else -> return@run failure(ApiError.InvalidField(
                        "User already has the role",
                        "The user already has the specified role to add",
                    ))
                }

            return@run success(success)
        }
    fun updateUserCategory(userId: Int, categoryId: Int): Either<ApiError, Boolean> =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val categoryRepository = it.categoryRepository
            val categoryDirRepository = it.categoryDirRepository

            categoryRepository.getCategoryNameById(categoryId) ?: return@run failure(ApiError.NotFound(
                "Category not found",
                "The provided category does not exist",
            ))
            usersRepository.getUserById(userId) ?: return@run failure(userNotFoundId)

            val success: Boolean = categoryDirRepository.updateUserCategory(userId, categoryId)
            return@run success(success)
        }

    fun validateUser(
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        birthDate: String,
        iban: String,
    ): Either<ApiError, Unit> {
        if (!name.contains(" ")) return failure(ApiError.InvalidField(
            "Name must contain at least a first and last name",
            "The provided name does not contain a space between first and last name.",
        ))
        if (!utilsDomain.validName(name)) return failure(invalidFieldError("name"))
        if (!utilsDomain.validPhoneNumber(phoneNumber)) return failure(invalidFieldError("phone number"))
        if (!utilsDomain.validAddress(address)) return failure(invalidFieldError("address"))
        if (!utilsDomain.validEmail(email)) return failure(invalidFieldError("email"))
        if (!usersUtils.validBirthDate(birthDate)) return failure(invalidFieldError("birth date"))
        if (!usersUtils.validatePortugueseIban(iban)) return failure(invalidFieldError("IBAN"))

        return success(Unit)
    }

    // TODO check if it makes sense to create a separate service for this
    fun getAllRoles(): Either<ApiError, List<Role>> =
        transactionManager.run {
            val roleRepository = it.roleRepository
            val roles = roleRepository.getAllRoles()
            if (roles.isEmpty()) return@run failure(ApiError.NotFound(
                "No roles found",
                "There are no roles available in the system.",
            )) // TODO check if this is the right error
            return@run success(roles)
        }

    fun inUseError (field : String): ApiError =
        ApiError.InvalidField(
            "$field in use",
            "The $field is already in use by another user. Please choose a different one.",
        )

    fun invalidFieldError(field: String): ApiError =
        ApiError.InvalidField(
            "Invalid $field",
            "The provided $field is invalid. Please check the format and try again.",
        )
}
