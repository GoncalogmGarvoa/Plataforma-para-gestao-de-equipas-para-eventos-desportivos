package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.repository.CallListRepository
import java.time.LocalDate

class CallListRepositoryJdbi(
    private val handle: Handle,
) : CallListRepository {
    override fun createCallList(
        deadline: LocalDate,
        userId: Int,
        competitionId: Int,
        callType: String,
    ): Int =
        handle
            .createUpdate(
                """insert into dbp.call_list (deadline, user_id, competition_id,call_type) values (:deadline, :user_id, :competition_id,:callType)""",
            ).bind("deadline", deadline)
            .bind("user_id", userId)
            .bind("competition_id", competitionId)
            .bind("callType", callType)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single()

    override fun updateCallList(
        id: Int,
        deadline: LocalDate,
        callType: String,
        competitionId: Int
    ): Int =
        handle
            .createUpdate(
                """update dbp.call_list set deadline = :deadline, call_type = :call_type, competition_id = :competition_id where id = :id""",
            ).bind("id", id)
            .bind("deadline", deadline)
            .bind("call_type", callType)
            .bind("competition_id", competitionId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single()


    override fun updateCallListStatus(callListId: Int): Boolean {
        handle
            .createUpdate(
                """update dbp.call_list set call_type = 'confirmation' where id = :call_list_id""",
            ).bind("call_list_id", callListId)
            .execute()
        return true
    }

    override fun getCallListById(id: Int): CallList? =
        handle
            .createQuery(
                """select * from dbp.call_list where id = :id""",
            ).bind("id", id)
            .mapTo<CallList>()
            .singleOrNull()

    override fun updateCallListStage(
        callListId: Int,
        callType: String,
    ): CallList {
        handle
            .createUpdate(
                """update dbp.call_list set call_type = :callType where id = :call_list_id""",
            ).bind("call_list_id", callListId)
            .bind("callType", callType)
            .execute()
        return getCallListById(callListId) ?: throw IllegalStateException("CallList not found")
    }
}

//    override fun findCallListById(id: Int): CallList? =
//        handle
//            .createQuery("""select * from dbp.call_list where id = :id""")
//            .bind("id", id)
//            .mapTo<CallList>()
//            .singleOrNull()
//
//    override fun getCallListsByCouncil(councilId: Int): List<CallList> =
//        handle
//            .createQuery("""select * from dbp.call_list where council_id = :council_id""")
//            .bind("council_id", councilId)
//            .mapTo<CallList>()
//            .list()
//
//    override fun deleteCallList(id: Int): Boolean =
//        handle
//            .createUpdate("""delete from dbp.call_list where id = :id""")
//            .bind("id", id)
//            .execute() > 0
//
//    override fun updateCallList(
//        id: Int,
//        deadline: LocalDate,
//        callType: String,
//        councilId: Int,
//        competitionId: Int,
//    ): Boolean =
//        handle
//            .createUpdate(
//                """update dbp.call_list set deadline = :deadline, call_type = :call_type, council_id = :council_id, competition_id = :competition_id where id = :id""",
//            ).bind("id", id)
//            .bind("deadline", deadline)
//            .bind("call_type", callType)
//            .bind("council_id", councilId)
//            .bind("competition_id", competitionId)
//            .execute() > 0
//
//    override fun getCallListReferees(callListId: Int): List<Referee> =
//        handle
//            .createQuery("""select * from dbp.referees where call_list_id = :call_list_id""")
//            .bind("call_list_id", callListId)
//            .mapTo<Referee>()
//            .list()
