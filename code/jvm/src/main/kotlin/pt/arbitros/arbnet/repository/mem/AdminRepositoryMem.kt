package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.repository.AdminRepository

class AdminRepositoryMem : AdminRepository {
    private val adminUserIds = mutableSetOf<Int>()

    override fun createAdmin(userId: Int): Boolean =
        adminUserIds.add(userId)

    override fun deleteAdmin(userId: Int): Boolean =
        adminUserIds.remove(userId)
}
