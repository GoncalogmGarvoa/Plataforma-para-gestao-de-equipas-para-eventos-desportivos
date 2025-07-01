@file:Suppress("ktlint:standard:filename", "ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.repository.mem

import kotlinx.datetime.Instant
import pt.arbitros.arbnet.domain.users.*
import pt.arbitros.arbnet.repository.UsersRepository
import java.time.LocalDate

class UsersRepositoryMem : UsersRepository {
    private val users = mutableListOf<User>()
    private var nextId = 1

    override fun getTokenAndUserByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? {
        TODO("Not yet implemented")
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Token? {
        TODO("Not yet implemented")
    }

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        TODO("Not yet implemented")
    }


    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        TODO("Not yet implemented")
    }

    override fun assignRoleToUserToToken(userId: Int, tokenId: TokenValidationInfo, roleId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        TODO("Not yet implemented")
    }

    override fun getUserByToken(tokenValidationInfo: TokenValidationInfo): User? {
        TODO("Not yet implemented")
    }



    override fun createUser(
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
        birthDate: LocalDate,
        iban: String,
    ): Int {
        val user =
            User(
                id = nextId++,
                name = name,
                phoneNumber = phoneNumber,
                address = address,
                email = email,
                passwordValidation = passwordValidation,
                birthDate = birthDate,
                iban = iban,
                userStatus = UserStatus.ACTIVE,
                // roles = emptyList(),
            )
        users.add(user)
        return user.id
    }

    override fun getUserById(id: Int): User? = users.find { it.id == id }

    override fun getUserByEmail(email: String): User? = users.find { it.email == email }

    override fun getUsersByName(name: String): List<User> {
        TODO("Not yet implemented")
    }

    override fun existsByEmail(email: String): Boolean = users.any { it.email == email }

    override fun existsByEmailExcludingId(
        email: String,
        id: Int,
    ): Boolean {
        val user = users.find { it.email == email }
        return user != null && user.id != id
    }

    override fun existsByPhoneNumber(phoneNumber: String): Boolean = users.any { it.phoneNumber == phoneNumber }

    override fun existsByPhoneNumberExcludingId(
        phoneNumber: String,
        id: Int,
    ): Boolean {
        val user = users.find { it.phoneNumber == phoneNumber }
        return user != null && user.id != id
    }

    override fun existsByIban(iban: String): Boolean = users.any { it.iban == iban }

    override fun existsByIbanExcludingId(
        iban: String,
        id: Int,
    ): Boolean {
        val user = users.find { it.iban == iban }
        return user != null && user.id != id
    }

    override fun updateUser(
        index: Int,
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        password: PasswordValidationInfo,
        birthDate: LocalDate,
        iban: String,
    ): Boolean {
        val existing = users[index]
        users[index] =
            existing.copy(
                name = name,
                phoneNumber = phoneNumber,
                address = address,
                passwordValidation = password,
                birthDate = birthDate,
                iban = iban,
            )
        return true
    }

    override fun getUsersByParameters(
        name: String,
        roles: List<String>
    ): List<User> {
        TODO("Not yet implemented")
    }

    override fun getUsersWithoutRoles(name: String): List<User> {
        TODO("Not yet implemented")
    }

    override fun updateRoles(
        userId: Int,
        roles: List<String>,
    ): Boolean {
        TODO("Not yet implemented")
//        val index = users.indexOfFirst { it.id == userId }
//        if (index == -1) return false
//
//        val user = users[index]
//        users[index] = user.copy(roles = roles)
//        return true
    }

    override fun deleteUser(id: Int): Boolean = users.removeIf { it.id == id }

    override fun userHasCouncilRole(councilUserId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getUsersAndCheckIfReferee(participants: List<Int>): List<User> {
        TODO("Not yet implemented")
    }
}
