package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.UsersRolesRepository

class UsersRolesRepositoryJdbi(
    private val handle: Handle,
) : UsersRolesRepository {
    override fun userHasRole(id: Int, roleId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun addRoleToUser(id: Int, roleId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeRoleFromUser(id: Int, roleId: Int): Boolean {
        TODO("Not yet implemented")
    }

}