package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.repository.FunctionRepository

class FunctionRepositoryJdbi(
    private val handle: Handle,
) : FunctionRepository {
    override fun getFunctionIdByName(functionName: String): Int? =
        handle
            .createQuery(
                "select id from dbp.function where name = :function_name",
            ).bind("function_name", functionName)
            .mapTo<Int>()
            .singleOrNull()

    override fun getFunctionNameById(functionId: Int): String =
        handle
            .createQuery(
                "select name from dbp.function where id = :role_id",
            ).bind("role_id", functionId)
            .mapTo<String>()
            .single()

    override fun getFunctionIds(functions: List<String>): List<Int> =
        handle
            .createQuery(
                """select id from dbp.function where name in (<functions>)""",
            ).bindList("functions", functions)
            .mapTo<Int>()
            .list() as List<Int>
}
