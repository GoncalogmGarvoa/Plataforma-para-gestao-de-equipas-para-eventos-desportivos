package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Role

interface RoleRepository {
    fun getRoleName(roleId: Int) : String?
    fun getAllRoles(): List<Role>
}