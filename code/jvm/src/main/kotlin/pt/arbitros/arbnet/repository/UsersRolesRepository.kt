package pt.arbitros.arbnet.repository

interface UsersRolesRepository {

    fun userHasRole(id: Int, roleId: Int) : Boolean
    fun addRoleToUser   (id: Int, roleId: Int) : Boolean
    fun removeRoleFromUser(id: Int, roleId: Int) : Boolean

}
