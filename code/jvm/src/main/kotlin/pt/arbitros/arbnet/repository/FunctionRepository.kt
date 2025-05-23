package pt.arbitros.arbnet.repository

interface FunctionRepository {
    fun getFunctionIdByName(roleName: String): Int?

    fun getFunctionNameById(roleId: Int): String

    fun getFunctionIds(functions: List<String>): List<Int>
}
