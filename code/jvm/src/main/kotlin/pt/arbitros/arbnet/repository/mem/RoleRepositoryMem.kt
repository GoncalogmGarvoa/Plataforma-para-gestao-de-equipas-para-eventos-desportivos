package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.repository.RoleRepository

class RoleRepositoryMem : RoleRepository {
    private val roles = mutableMapOf<Int, String>()

    override fun getRoleName(roleId: Int): String? {
        return roles[roleId]
    }


}