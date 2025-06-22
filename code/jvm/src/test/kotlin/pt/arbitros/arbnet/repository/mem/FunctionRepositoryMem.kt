package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.repository.adaptable_repos.FunctionRepository

class FunctionRepositoryMem : FunctionRepository {
    private val roles = mutableMapOf<Int, String>()
    private var nextId = 1

    fun addRole(roleName: String): Int {
        val existing = roles.entries.find { it.value == roleName }
        if (existing != null) return existing.key

        val id = nextId++
        roles[id] = roleName
        return id
    }

    override fun getFunctionIdByName(roleName: String): Int =
        roles.entries.find { it.value == roleName }?.key
            ?: throw NoSuchElementException("Role with name '$roleName' not found")

    override fun getFunctionNameById(roleId: Int): String = roles[roleId] ?: throw NoSuchElementException("Role with ID '$roleId' not found")
    override fun verifyFunctionIds(functions: List<String>): List<Int> {
        val ids = mutableListOf<Int>()
        for (function in functions) {
            val id = roles.entries.find { it.value == function }?.key
            if (id != null) {
                ids.add(id)
            }
        }
        return ids
    }
}
