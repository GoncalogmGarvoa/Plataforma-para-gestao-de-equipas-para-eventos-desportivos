package pt.arbitros.arbnet.repository

interface RoleRepository {
    fun getRoleIdByName(roleName: String): Int

    fun getRoleNameById(roleId: Int): String
}
