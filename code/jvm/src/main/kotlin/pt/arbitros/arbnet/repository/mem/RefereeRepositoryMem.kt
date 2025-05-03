package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.ArbitrationCouncil
import pt.arbitros.arbnet.domain.Referee
import pt.arbitros.arbnet.repository.RefereeRepository


class RefereeRepositoryMem : RefereeRepository {
    private val referees = mutableMapOf<Int, Referee>()
    private var nextId = 1

    override fun createReferee(userId: Int): Boolean {
        if (referees.containsKey(userId)) return false
        referees[userId] = Referee(userId = nextId++)
        return true
    }

    override fun getAllReferees(refereeIds: List<Int>): List<Referee> =
        refereeIds.mapNotNull { referees[it] }

    override fun deleteReferee(userId: Int): Boolean =
        referees.remove(userId) != null
}
