package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.repository.FunctionRepository

class FunctionRepositoryJdbi(
    private val handle: Handle
) : FunctionRepository {

    override fun getFunctionIdByName(roleName: String): Int? =
        handle
            .createQuery(
                "select id from dbp.role where name = :role_name",
            ).bind("role_name", roleName)
            .mapTo<Int>()
            .singleOrNull()

    override fun getFunctionNameById(roleId: Int): String =
        handle
            .createQuery(
                "select name from dbp.role where id = :role_id",
            ).bind("role_id", roleId)
            .mapTo<String>()
            .single()

    override fun getFunctionIds(functions: List<String>): List<Int> =
        handle
            .createQuery("""select id from dbp.function where name in (<functions>)""",
            )
            .bindList("functions", functions)
            .mapTo<Int>()
            .list() as List<Int>
}