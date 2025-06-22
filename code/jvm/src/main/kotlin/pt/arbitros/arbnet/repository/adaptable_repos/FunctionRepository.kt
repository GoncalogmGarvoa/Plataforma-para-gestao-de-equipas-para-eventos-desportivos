package pt.arbitros.arbnet.repository.adaptable_repos

import pt.arbitros.arbnet.domain.universal.Function

interface FunctionRepository {

    fun getAllFunctions(): List<Function>

    fun getFunctionIdByName(roleName: String): Int?

    fun getFunctionNameById(roleId: Int): String

    fun getFunctionIds(functions: List<String>): List<Int>
}