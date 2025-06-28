package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.domain.CallListWithUserAndCompetition
import pt.arbitros.arbnet.http.model.EventOutputModel
import pt.arbitros.arbnet.http.model.RefereeCallLists
import pt.arbitros.arbnet.http.model.RefereeCallListsOutputModel
import pt.arbitros.arbnet.repository.CallListRepository
import java.time.LocalDate

class CallListRepositoryMem : CallListRepository {
    private val callLists = mutableListOf<CallList>()
    private var nextId = 1

    override fun createCallList(
        deadline: LocalDate,
        userId: Int,
        competitionId: Int,
        callType: String,
    ): Int {
        val callList =
            CallList(
                id = nextId++,
                deadline = deadline,
                callType = "callList",
                userId = userId,
                competitionId = competitionId,
            )
        callLists.add(callList)
        return callList.id
    }

    override fun updateCallList(
        id: Int,
        deadline: LocalDate,
        callType: String,
        competitionId: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun getCallListsByUserIdAndType(userId: Int, type: String): List<CallListWithUserAndCompetition> {
        TODO("Not yet implemented")
    }

    override fun updateCallListStatus(callListId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCallListsWithReferee(userId: Int): List<RefereeCallLists> {
        TODO("Not yet implemented")
    }

    override fun getCallListById(id: Int): CallList? {
        TODO("Not yet implemented")
    }

    override fun updateCallListStage(
        callListId: Int,
        callType: String,
    ): CallList {
        TODO("Not yet implemented")
    }
}
