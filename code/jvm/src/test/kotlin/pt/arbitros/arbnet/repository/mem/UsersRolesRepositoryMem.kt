package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.repository.UsersRolesRepository

class UsersRolesRepositoryMem : UsersRolesRepository {
    private val userRoles = mutableMapOf<Int, MutableSet<Int>>()

    override fun userHasRole(
        userId: Int,
        roleId: Int,
    ): Boolean = userRoles[userId]?.contains(roleId) ?: false

    override fun getUserRolesId(userId: Int): List<Int> {
        TODO("Not yet implemented")
    }

    override fun addRoleToUser(
        userId: Int,
        roleId: Int,
    ): Boolean = userRoles.getOrPut(userId) { mutableSetOf() }.add(roleId)

    override fun removeRoleFromUser(
        userId: Int,
        roleId: Int,
    ): Boolean = userRoles[userId]?.remove(roleId) ?: false
}
