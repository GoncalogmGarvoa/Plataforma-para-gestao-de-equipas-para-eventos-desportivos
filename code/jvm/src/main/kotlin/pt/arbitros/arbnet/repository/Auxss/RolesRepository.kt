package pt.arbitros.arbnet.repository.Auxss

import pt.arbitros.arbnet.domain.Role

interface RolesRepository {
    fun createRole(role: Role): Int

    fun getRoleById(id: Int): Role?

    fun getAllRoles(): List<Role>

    fun deleteRole(id: Int): Boolean
}
