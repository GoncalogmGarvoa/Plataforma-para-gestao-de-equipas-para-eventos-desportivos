package pt.arbitros.arbnet.repository.adaptable_repos

import pt.arbitros.arbnet.domain.adaptable.Function

interface FunctionRepository {

    fun getAllFunctions(): List<Function>

    fun getFunctionIdByName(roleName: String): Int?

    fun getFunctionNameById(functionId: Int): String?

    fun verifyFunctionIds(functions: List<String>): List<Int>

    fun verifyFunctionNames(functions: List<String>): Boolean
}