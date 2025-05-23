package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.repository.UsersRolesRepository

class UsersRolesRepositoryJdbi(
    private val handle: Handle,
) : UsersRolesRepository {
    override fun userHasRole(
        userId: Int,
        roleId: Int,
    ): Boolean =
        handle
            .createQuery(
                """select * from dbp.users_roles where user_id = :id and role_id = :roleId""",
            ).bind("id", userId)
            .bind("roleId", roleId)
            .mapTo<Int>()
            .findFirst()
            .isPresent

    override fun getUserRolesId(userId: Int): List<Int> =
        handle
            .createQuery(
                """select role_id from dbp.users_roles where user_id = :id""",
            ).bind("id", userId)
            .mapTo<Int>()
            .list()

    override fun addRoleToUser(
        userId: Int,
        roleId: Int,
    ): Boolean =
        handle
            .createUpdate(
                """insert into dbp.users_roles (user_id, role_id) values (:id, :roleId)""",
            ).bind("id", userId)
            .bind("roleId", roleId)
            .execute() > 0

    override fun removeRoleFromUser(
        userId: Int,
        roleId: Int,
    ): Boolean =
        handle
            .createUpdate(
                """delete from dbp.users_roles where user_id = :id and role_id = :roleId""",
            ).bind("id", userId)
            .bind("roleId", roleId)
            .execute() > 0
}
