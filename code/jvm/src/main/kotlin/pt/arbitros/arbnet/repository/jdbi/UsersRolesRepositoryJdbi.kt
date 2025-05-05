package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.UsersRolesRepository

class UsersRolesRepositoryJdbi(
    private val handle: Handle,
) : UsersRolesRepository {
    override fun userHasRole(
        userId: Int,
        roleId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun addRoleToUser(
        userId: Int,
        roleId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeRoleFromUser(
        userId: Int,
        roleId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }
}
