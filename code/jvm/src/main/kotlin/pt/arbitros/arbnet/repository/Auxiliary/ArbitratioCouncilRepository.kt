package pt.arbitros.arbnet.repository.Aux

import pt.arbitros.arbnet.domain.ArbitrationCouncil

interface ArbitrationCouncilRepository {
    fun createCouncilMember(arbitrationCouncil: ArbitrationCouncil): Boolean

    fun findCouncilMemberById(userId: Int): ArbitrationCouncil?

    fun deleteCouncilMember(userId: Int): Boolean

    fun getAllCouncilMembers(): List<ArbitrationCouncil>
}
