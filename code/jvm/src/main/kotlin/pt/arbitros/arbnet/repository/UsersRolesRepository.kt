package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.adaptable.Role

interface UsersRolesRepository {
    fun userHasRole(
        userId: Int,
        roleId: Int,
    ): Boolean

    fun getUserRolesId(userId: Int): List<Int>

    fun getUsersRolesName(userId: Int): List<String>

    fun addRoleToUser(
        userId: Int,
        roleId: Int,
    ): Boolean

    fun removeRoleFromUser(
        userId: Int,
        roleId: Int,
    ): Boolean

    fun getAdminUsers(): List<Int>

    fun getRoleByToken(tokenId: String): Role?
}
