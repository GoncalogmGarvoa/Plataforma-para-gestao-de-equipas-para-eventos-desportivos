package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.adaptable.Role
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

    override fun getUsersRolesName(userId: Int): List<String> =
        handle
            .createQuery(
                """select r.name from dbp.users_roles ur join dbp.role r on ur.role_id = r.id where ur.user_id = :id""",
            ).bind("id", userId)
            .mapTo<String>()
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

    override fun getAdminUsers(): List<Int> =
        handle
            .createQuery(
                """select user_id from dbp.users_roles where role_id = 1""",
            )
            .mapTo<Int>()
            .list()

    override fun getRoleByToken(tokenId: String): Role? {
        return handle
            .createQuery(
                """
                select r.id, r.name from dbp.user_token_role as utr
                inner join dbp.role as r on utr.role_id = r.id
                where utr.token_val = :token_id
            """,
            ).bind("token_id", tokenId)
            .map { rs, _ ->
                Role(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                )
            }.singleOrNull()
    }
}
