package pt.arbitros.arbnet.repository

interface UsersRolesRepository {
    fun userHasRole(
        userId: Int,
        roleId: Int,
    ): Boolean

    fun getUserRolesId(userId: Int): List<Int>

    fun addRoleToUser(
        userId: Int,
        roleId: Int,
    ): Boolean

    fun removeRoleFromUser(
        userId: Int,
        roleId: Int,
    ): Boolean
}
