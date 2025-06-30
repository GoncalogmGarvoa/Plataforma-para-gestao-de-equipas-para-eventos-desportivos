package pt.arbitros.arbnet.repository.jdbi.adaptable_repos

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.adaptable.Function
import pt.arbitros.arbnet.repository.adaptable_repos.FunctionRepository

class FunctionRepositoryJdbi(
    private val handle: Handle,
) : FunctionRepository {

    override fun getAllFunctions(): List<Function> {
        return handle
            .createQuery(
                "select id, name from dbp.function",
            ).mapTo<Function>()
            .list()
    }

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

    override fun verifyFunctionIds(functions: List<String>): List<Int> {
        val sql = """
        select id from dbp.function
        where name in (<FUNCTIONS>)
        """

        return handle.createQuery(sql)
            .bindList("FUNCTIONS", functions)
            .mapTo(Int::class.java)
            .list()
    }

}