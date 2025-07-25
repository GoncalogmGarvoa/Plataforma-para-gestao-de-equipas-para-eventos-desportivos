package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.domain.CallListWithUserAndCompetition
import pt.arbitros.arbnet.http.model.RefereeCallLists
import java.time.LocalDate

interface CallListRepository {


    fun createCallList(
        deadline: LocalDate,
        userId: Int,
        competitionId: Int,
        callType: String,
    ): Int

    fun deleteCallList(id: Int): Boolean

    fun updateCallList(
        id: Int,
        deadline: LocalDate,
        callType: String,
        competitionId: Int,
    ): Int

//    fun getCallListsByUserIdAndType(
//        userId: Int,
//        type: String
//    ): List<CallListWithUserAndCompetition>

    fun updateCallListStatus(callListId: Int): Boolean

    fun getCallListsWithReferee(userId: Int ,limit: Int, offset: Int): List<RefereeCallLists>

    fun getCallListById(id: Int): CallList?

    fun updateCallListStage(
        callListId: Int,
        callType: String,
    ): CallList

    fun getCallListsByCompetitionId(competitionId: Int): CallList?

    fun getCallListsFinalJuryFunction(userId: Int, callListType: String, functionId:Int ): List<CallList>

//    fun getCallListsByCouncil(councilId: Int): List<CallList>
//
//    fun deleteCallList(id: Int): Boolean


    fun countCallListsWithReferee(userId: Int): Int
    fun getCallListsByUserIdAndType(
        userId: Int,
        type: String,
        limit: Int,
        offset: Int
    ): List<CallListWithUserAndCompetition>
}
