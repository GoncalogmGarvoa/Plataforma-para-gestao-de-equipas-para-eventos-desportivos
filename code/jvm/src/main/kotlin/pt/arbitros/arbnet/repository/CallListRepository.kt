package pt.arbitros.arbnet.repository

import java.time.LocalDate

interface CallListRepository {
    fun createCallList(
        deadline: LocalDate,
        councilId: Int,
        competitionId: Int,
    ): Int

    fun updateCallListStatus(callListId: Int) : Boolean


//    fun findCallListById(id: Int): CallList?
//
//    fun getCallListsByCouncil(councilId: Int): List<CallList>
//
//    fun deleteCallList(id: Int): Boolean
//
//    fun updateCallList(
//        id: Int,
//        deadline: LocalDate,
//        callType: String,
//        councilId: Int,
//        competitionId: Int,
//    ): Boolean
//
//    fun getCallListReferees(callListId: Int): List<Referee>
}
