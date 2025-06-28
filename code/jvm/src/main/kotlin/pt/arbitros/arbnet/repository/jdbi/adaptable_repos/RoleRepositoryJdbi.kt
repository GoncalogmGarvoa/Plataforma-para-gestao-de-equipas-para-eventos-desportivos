package pt.arbitros.arbnet.repository.jdbi.adaptable_repos

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.adaptable.Role
import pt.arbitros.arbnet.repository.adaptable_repos.RoleRepository

class RoleRepositoryJdbi(
    private val handle: Handle,
) : RoleRepository {
    override fun getRoleName(roleId: Int): String? =
        handle
            .createQuery(
                """select name from dbp.role where id = :roleId""",
            ).bind("roleId", roleId)
            .mapTo<String>()
            .singleOrNull()

    override fun getRoleId(roleName: String): Int? {
        return handle
            .createQuery(
                """select id from dbp.role where name = :roleName""",
            ).bind("roleName", roleName)
            .mapTo<Int>()
            .singleOrNull()
    }

    override fun getAllRoles(): List<Role> =
        handle
            .createQuery(
                """select * from dbp.role""",
            ).mapTo<Role>()
            .list()
}