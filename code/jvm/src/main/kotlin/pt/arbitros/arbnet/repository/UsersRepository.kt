package pt.arbitros.arbnet.repository

import kotlinx.datetime.Instant
import pt.arbitros.arbnet.domain.users.*
import java.time.LocalDate

interface UsersRepository {
    fun getTokenAndUserByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Token?


    fun createToken(
        token: Token,
        maxTokens: Int,
    )


    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun assignRoleToUserToToken(
        userId: Int,
        tokenId: TokenValidationInfo,
        roleId: Int,
    ): Boolean

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int

    fun getUserByToken(tokenValidationInfo: TokenValidationInfo): User?

    fun createUser(
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
        birthDate: LocalDate,
        iban: String,
    ): Int

    fun getUserById(id: Int): User?

    fun getUserByEmail(email: String): User?

    fun getUsersByName(name: String): List<User>

    fun existsByEmail(email: String): Boolean

    fun existsByEmailExcludingId(
        email: String,
        id: Int,
    ): Boolean

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByPhoneNumberExcludingId(
        phoneNumber: String,
        id: Int,
    ): Boolean

    fun existsByIban(iban: String): Boolean

    fun existsByIbanExcludingId(
        iban: String,
        id: Int,
    ): Boolean

    fun updateUser(
        id: Int,
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        password: PasswordValidationInfo,
        birthDate: LocalDate,
        iban: String,
    ): Boolean

    fun getUsersByParameters(
        name: String,
        roles: List<String>,
    ): List<User>

    fun getUsersWithoutRoles(
        name: String,
    ): List<User>

    fun updateRoles(
        userId: Int,
        roles: List<String>,
    ): Boolean

    fun deleteUser(id: Int): Boolean

    fun userHasCouncilRole(userId: Int): Boolean

    fun getUsersAndCheckIfReferee(participants: List<Int>): List<User>
}
