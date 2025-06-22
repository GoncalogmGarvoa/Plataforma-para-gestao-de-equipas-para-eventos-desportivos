package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.CallList
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

    fun updateCallListStatus(callListId: Int): Boolean

    fun getCallListById(id: Int): CallList?

    fun updateCallListStage(
        callListId: Int,
        callType: String,
    ): CallList


//    fun getCallListsByCouncil(councilId: Int): List<CallList>
//
//    fun deleteCallList(id: Int): Boolean


}
