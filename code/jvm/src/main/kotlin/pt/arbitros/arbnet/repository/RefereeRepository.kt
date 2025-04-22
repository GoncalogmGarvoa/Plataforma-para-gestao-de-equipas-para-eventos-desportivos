package pt.arbitros.arbnet.repository

interface RefereeRepository {
    fun checkIfRefereesAreActive(userId: Int): List<Int>
}
