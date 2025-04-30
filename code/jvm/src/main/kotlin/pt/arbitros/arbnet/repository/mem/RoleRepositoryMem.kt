package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.repository.RoleRepository

class RoleRepositoryMem : RoleRepository {
    private val roles = mutableMapOf<Int, String>()
    private var nextId = 1

    fun addRole(roleName: String): Int {
        val existing = roles.entries.find { it.value == roleName }
        if (existing != null) return existing.key

        val id = nextId++
        roles[id] = roleName
        return id
    }

    override fun getRoleIdByName(roleName: String): Int =
        roles.entries.find { it.value == roleName }?.key
            ?: throw NoSuchElementException("Role with name '$roleName' not found")

    override fun getRoleNameById(roleId: Int): String = roles[roleId] ?: throw NoSuchElementException("Role with ID '$roleId' not found")
}
