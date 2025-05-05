package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.repository.UsersRolesRepository

class UsersRolesRepositoryMem : UsersRolesRepository {
    private val userRoles = mutableMapOf<Int, MutableSet<Int>>()

    override fun userHasRole(id: Int, roleId: Int): Boolean {
        return userRoles[id]?.contains(roleId) ?: false
    }

    override fun addRoleToUser(id: Int, roleId: Int): Boolean {
        return userRoles.getOrPut(id) { mutableSetOf() }.add(roleId)
    }

    override fun removeRoleFromUser(id: Int, roleId: Int): Boolean {
        return userRoles[id]?.remove(roleId) ?: false
    }
}