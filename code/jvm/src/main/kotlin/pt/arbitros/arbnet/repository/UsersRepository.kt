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

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByIban(iban: String): Boolean

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
        id: Int,
        roles: List<String>,
    ): Boolean

    fun deleteUser(id: Int): Boolean
}
