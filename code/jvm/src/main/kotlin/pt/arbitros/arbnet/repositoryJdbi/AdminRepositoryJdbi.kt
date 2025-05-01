package pt.arbitros.arbnet.repositoryJdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.domain.Admin
import pt.arbitros.arbnet.repository.AdminRepository

class AdminRepositoryJdbi (
    private val handle: Handle
) : AdminRepository {

    override fun createAdmin(userId: Int): Boolean =
        handle.createUpdate(
            "INSERT INTO admin (user_id) VALUES (:userId)"
        )
            .bind("userId", userId)
            .execute() > 0


    override fun deleteAdmin(userId: Int): Boolean =
        handle.createUpdate(
            """ 
            DELETE FROM admin WHERE user_id = :userId"""
        )
            .bind("userId", userId)
            .execute() > 0
}
