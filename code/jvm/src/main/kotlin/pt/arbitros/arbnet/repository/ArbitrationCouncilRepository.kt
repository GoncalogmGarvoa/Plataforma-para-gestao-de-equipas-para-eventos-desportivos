package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.ArbitrationCouncil

interface ArbitrationCouncilRepository {
    fun createCouncilMember(userId: Int): Boolean

    fun getCouncilMemberById(userId: Int): ArbitrationCouncil?

    fun deleteCouncilMember(userId: Int): Boolean

    //fun getAllCouncilMembers(): List<ArbitrationCouncil>
}
