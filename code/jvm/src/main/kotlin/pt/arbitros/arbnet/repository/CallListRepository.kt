package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.domain.Referee
import java.time.LocalDate

interface CallListRepository {
    fun createCallList(
    /*  Porque colocar os atributos que estao em competição repetidos?
        competitionName: String,
        competitionNumber: Int,
        address: String,
        phoneNumber: Int,
        email: String,
        association: String,
        location: String,
        participant: List<String>, //possivel de meter
        timeLine: LocalDate,
        type: String,
    ): Int */
        deadline: LocalDate,
        callType: String,
        councilId: Int,
        competitionId: Int,
    ): Int

    fun findCallListById(id: Int): CallList?

    fun getCallListsByCouncil(councilId: Int): List<CallList>

    fun deleteCallList(id: Int): Boolean

    fun updateCallList(
        id: Int,
        deadline: LocalDate,
        callType: String,
        councilId: Int,
        competitionId: Int,
    ): Boolean

    fun getCallListReferees(callListId: Int): List<Referee>
}
