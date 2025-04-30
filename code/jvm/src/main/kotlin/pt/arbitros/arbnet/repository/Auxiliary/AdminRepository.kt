package pt.arbitros.arbnet.repository.Aux

import pt.arbitros.arbnet.domain.Admin

interface AdminRepository {
    fun createAdmin(admin: Admin): Boolean

    fun findAdminById(userId: Int): Admin?

    fun deleteAdmin(userId: Int): Boolean
}
