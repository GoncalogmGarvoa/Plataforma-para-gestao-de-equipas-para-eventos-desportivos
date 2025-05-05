package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.repository.CallListRepository
import java.time.LocalDate

class CallListRepositoryMem : CallListRepository {
    private val callLists = mutableListOf<CallList>()
    private var nextId = 1

    override fun createCallList(
        deadline: LocalDate,
        userId: Int,
        competitionId: Int,
    ): Int {
        val callList =
            CallList(
                id = nextId++,
                deadline = deadline,
                callType = "callList",
                councilId = userId,
                competitionId = competitionId,
            )
        callLists.add(callList)
        return callList.id
    }

    override fun updateCallListStatus(callListId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCallListById(id: Int): CallList? {
        TODO("Not yet implemented")
    }
}
