package pt.arbitros.arbnet.repository.adaptable_repos

import pt.arbitros.arbnet.domain.adaptable.Role

interface RoleRepository {
    fun getRoleName(roleId: Int): String?

    fun getRoleId(roleName: String): Int?

    fun getAllRoles(): List<Role>
}