package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.CallList

interface CallListRepository {

    fun createCallList(callList: CallList): Int

    fun findCallListById(id: Int): CallList?

    fun getCallListsByCouncil(councilId: Int): List<CallList>

    fun deleteCallList(id: Int): Boolean
}
