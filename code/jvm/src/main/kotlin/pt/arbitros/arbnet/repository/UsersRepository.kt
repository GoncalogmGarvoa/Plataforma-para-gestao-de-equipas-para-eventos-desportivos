package pt.arbitros.arbnet.repository

import kotlinx.datetime.Instant
import pt.arbitros.arbnet.domain.users.PasswordValidationInfo
import pt.arbitros.arbnet.domain.users.Token
import pt.arbitros.arbnet.domain.users.TokenValidationInfo
import pt.arbitros.arbnet.domain.users.Users
import java.time.LocalDate

interface UsersRepository {
    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<Users, Token>?

    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int

    fun getUserByToken(token: String): Users?

    fun createUser(
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        password: PasswordValidationInfo,
        birthDate: LocalDate,
        iban: String,
    ): Int

    fun getUserById(id: Int): Users?

    fun getUserByEmail(email: String): Users?

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

    fun updateRoles(
        userId: Int,
        roles: List<String>,
    ): Boolean

    fun deleteUser(id: Int): Boolean

    fun userHasCouncilRole(userId: Int): Boolean

    fun getUsersAndCheckIfReferee(participants: List<Int>): List<Users>
}
