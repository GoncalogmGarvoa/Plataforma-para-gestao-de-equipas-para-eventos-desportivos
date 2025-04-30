package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.repository.CallListRepository
import java.time.LocalDate

class CallListRepositoryMem : CallListRepository {
    private val callLists = mutableListOf<CallList>()
    private var nextId = 1

    override fun createCallList(
        deadline: LocalDate,
        councilId: Int,
        competitionId: Int,
    ): Int {
        val callList =
            CallList(
                id = nextId++,
                deadline = deadline,
                callType = "callList",
                councilId = councilId,
                competitionId = competitionId,
            )
        callLists.add(callList)
        return callList.id
    }
}
