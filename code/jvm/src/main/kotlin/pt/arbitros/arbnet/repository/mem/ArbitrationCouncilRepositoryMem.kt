package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.ArbitrationCouncil
import pt.arbitros.arbnet.repository.Aux.ArbitrationCouncilRepository




class ArbitrationCouncilRepositoryMem : ArbitrationCouncilRepository {
    private val councilMembers = mutableMapOf<Int, ArbitrationCouncil>()
    private var nextId = 1

    override fun createCouncilMember(userId: Int): Boolean {
        if (councilMembers.containsKey(userId)) return false
        councilMembers[userId] = ArbitrationCouncil(userId = nextId++)
        return true
    }

    override fun getCouncilMemberById(userId: Int) =
        councilMembers[userId]

    override fun deleteCouncilMember(userId: Int): Boolean =
        councilMembers.remove(userId) != null
}
