package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.repository.RoleRepository

class RepositoryRoleJdbi( // TODO review if necessary
    private val handle: Handle,
) : RoleRepository {
    override fun getRoleIdByName(roleName: String): Int? =
        handle
            .createQuery(
                "select id from dbp.role where name = :role_name",
            ).bind("role_name", roleName)
            .mapTo<Int>()
            .singleOrNull()

    override fun getRoleNameById(roleId: Int): String =
        handle
            .createQuery(
                "select name from dbp.role where id = :role_id",
            ).bind("role_id", roleId)
            .mapTo<String>()
            .single()
}
