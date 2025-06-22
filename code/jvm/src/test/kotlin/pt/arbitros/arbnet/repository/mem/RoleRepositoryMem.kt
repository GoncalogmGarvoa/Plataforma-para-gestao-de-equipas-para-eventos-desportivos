package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.adaptable.Role
import pt.arbitros.arbnet.repository.adaptable_repos.RoleRepository

class RoleRepositoryMem : RoleRepository {
    private val roles = mutableMapOf<Int, String>()

    override fun getRoleName(roleId: Int): String? = roles[roleId]

    override fun getAllRoles(): List<Role> {
        TODO("Not yet implemented")
    }
}
