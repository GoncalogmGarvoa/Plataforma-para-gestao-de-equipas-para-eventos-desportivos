package pt.arbitros.arbnet.repository

interface RoleRepository {
    fun getRoleName(roleId: Int) : String?
}