package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Users

interface UsersRepository {

    fun createUser(user: Users): Int

    fun getUserById(id: Int): Users?

    fun findUserByEmail(email: String): Users?

    fun existsByEmail(email: String): Boolean

    fun updateUser(user: Users): Boolean

    fun deleteUser(id: Int): Boolean

    fun findRoles(user: Users): List<String>

    fun findIban(user: Users): Int
    fun updateIban(user: Users): Boolean



}
