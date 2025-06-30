package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.domain.CallListWithUserAndCompetition
import pt.arbitros.arbnet.http.model.RefereeCallLists
import pt.arbitros.arbnet.http.model.RefereeCallListsOutputModel
import java.time.LocalDate

interface CallListRepository {


    fun createCallList(
        deadline: LocalDate,
        userId: Int,
        competitionId: Int,
        callType: String,
    ): Int

    fun updateCallList(
        id: Int,
        deadline: LocalDate,
        callType: String,
        competitionId: Int,
    ): Int

    fun getCallListsByUserIdAndType(
        userId: Int,
        type: String
    ): List<CallListWithUserAndCompetition>

    fun updateCallListStatus(callListId: Int): Boolean

    fun getCallListsWithReferee(userId: Int): List<RefereeCallLists>

    fun getCallListById(id: Int): CallList?

    fun updateCallListStage(
        callListId: Int,
        callType: String,
    ): CallList


//    fun getCallListsByCouncil(councilId: Int): List<CallList>
//
//    fun deleteCallList(id: Int): Boolean


}
