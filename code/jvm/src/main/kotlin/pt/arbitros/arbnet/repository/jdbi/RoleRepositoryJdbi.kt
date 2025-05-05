package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.RoleRepository

class RoleRepositoryJdbi(
    private val handle: Handle,
) : RoleRepository {

    override fun getRoleName(roleId: Int): String? {
        TODO("Not yet implemented")
    }

}