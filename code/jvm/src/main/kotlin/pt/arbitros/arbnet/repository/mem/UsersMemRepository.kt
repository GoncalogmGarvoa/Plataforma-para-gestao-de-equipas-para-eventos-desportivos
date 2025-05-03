@file:Suppress("ktlint:standard:filename")

package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.repository.UsersRepository
import java.time.LocalDate

class UsersRepositoryMem : UsersRepository {
    private val users = mutableListOf<Users>()
    private var nextId = 1

    override fun createUser(
        name: String,
        phoneNumber: Int,
        address: String,
        email: String,
        password: String,
        birthDate: LocalDate,
        iban: String,
    ): Int {
        val user =
            Users(
                id = nextId++,
                name = name,
                phoneNumber = phoneNumber,
                address = address,
                email = email,
                password = password,
                birthDate = birthDate,
                iban = iban,
                roles = emptyList(),
            )
        users.add(user)
        return user.id
    }

    override fun getUserById(id: Int): Users? = users.find { it.id == id }

    override fun getUserByEmail(email: String): Users? = users.find { it.email == email }

    override fun existsByEmail(email: String): Boolean = users.any { it.email == email }

    override fun existsByPhoneNumber(phoneNumber: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun existsByIban(iban: String): Boolean {
        TODO("Not yet implemented")
    }


    override fun updateUser(
        index: Int,
        name: String,
        phoneNumber: Int,
        address: String,
        email: String,
        password: String,
        birthDate: LocalDate,
        iban: String,
    ): Boolean {

        val existing = users[index]
        users[index] =
            existing.copy(
                name = name,
                phoneNumber = phoneNumber,
                address = address,
                password = password,
                birthDate = birthDate,
                iban = iban,
            )
        return true
    }

    override fun updateRoles(
        id: Int,
        roles: List<String>,
    ): Boolean {
        val index = users.indexOfFirst { it.id == id }
        if (index == -1) return false

        val user = users[index]
        users[index] = user.copy(roles = roles)
        return true
    }

    override fun deleteUser(id: Int): Boolean = users.removeIf { it.id == id }
}
