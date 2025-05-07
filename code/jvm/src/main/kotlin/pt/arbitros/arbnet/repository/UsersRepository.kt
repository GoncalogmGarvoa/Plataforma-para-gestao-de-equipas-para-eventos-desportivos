package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Users
import java.time.LocalDate

interface UsersRepository {
    fun createUser(
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        password: String,
        birthDate: LocalDate,
        iban: String,
    ): Int

    fun getUserById(id: Int): Users?

    fun getUserByEmail(email: String): Users?

    fun existsByEmail(email: String): Boolean

    fun existsByEmailExcludingId(email: String, id: Int): Boolean

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByPhoneNumberExcludingId(phoneNumber: String, id: Int): Boolean

    fun existsByIban(iban: String): Boolean

    fun existsByIbanExcludingId(iban: String, id: Int): Boolean

    fun updateUser(
        id: Int,
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        password: String,
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
