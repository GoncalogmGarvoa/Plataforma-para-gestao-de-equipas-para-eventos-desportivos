package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.domain.CallListWithUserAndCompetition
import pt.arbitros.arbnet.http.model.RefereeCallLists
import pt.arbitros.arbnet.http.model.RefereeCallListsOutputModel
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

    override fun deleteCallList(id: Int): Boolean =
        handle
            .createUpdate(
                """delete from dbp.call_list where id = :id""",
            ).bind("id", id)
            .execute() > 0

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

    override fun getCallListsByUserIdAndType(
        userId: Int,
        type: String,
        offset: Int,
        limit: Int
    ): List<CallListWithUserAndCompetition> =
        handle.createQuery(
            """
        SELECT
            cl.id AS call_list_id,
            cl.deadline,
            cl.call_type,
            u.id AS user_id,
            u.name AS user_name,
            u.email AS user_email,
            c.competition_number AS competition_id,
            c.name AS competition_name
        FROM dbp.call_list cl
        JOIN dbp.users u ON cl.user_id = u.id
        JOIN dbp.competition c ON cl.competition_id = c.competition_number
        WHERE cl.user_id = :userId AND cl.call_type = :callType
        ORDER BY cl.deadline ASC
        LIMIT :limit OFFSET :offset
        """
        )
            .bind("userId", userId)
            .bind("callType", type)
            .bind("limit", limit)
            .bind("offset", offset)
            .mapTo<CallListWithUserAndCompetition>()
            .list()




//    override fun getCallListsByUserIdAndType(userId: Int, type: String): List<CallListWithUserAndCompetition> =
//        handle.createQuery(
//            """
//        SELECT
//            cl.id as call_list_id,
//            cl.deadline,
//            cl.call_type,
//            u.id as user_id,
//            u.name as user_name,
//            u.email as user_email,
//            c.competition_number as competition_id,
//            c.name as competition_name
//        FROM dbp.call_list cl
//        JOIN dbp.users u ON cl.user_id = u.id
//        JOIN dbp.competition c ON cl.competition_id = c.competition_number
//        WHERE cl.user_id = :userId AND cl.call_type = :callType
//        """
//        )
//            .bind("userId", userId)
//            .bind("callType", type)
//            .mapTo<CallListWithUserAndCompetition>()
//            .list()


override fun getCallListsWithReferee(userId: Int, offset: Int, limit: Int): List<RefereeCallLists> =
    handle.createQuery(
        """
        SELECT * FROM (
            SELECT 
                cl.id AS call_list_id,
                c.competition_number AS competition_id,
                c.name AS competition_name,
                c.address,
                c.phone_number, 
                c.email, 
                c.association,
                c.location,
                cl.deadline AS deadline,
                cl.call_type AS call_list_type,
                ROW_NUMBER() OVER (PARTITION BY cl.id ORDER BY cl.deadline ASC) as rn
            FROM dbp.call_list cl
            JOIN dbp.competition c ON cl.competition_id = c.competition_number
            JOIN dbp.participant p ON cl.id = p.call_list_id
            WHERE p.user_id = :userId AND cl.call_type != 'callList'
        ) AS ranked
        WHERE rn = 1
        ORDER BY ranked.deadline ASC
        LIMIT :limit OFFSET :offset
        """
    )
        .bind("userId", userId)
        .bind("limit", limit)
        .bind("offset", offset)
        .mapTo<RefereeCallLists>()
        .list()



    override fun countCallListsWithReferee(userId: Int): Int =
        handle.createQuery(
            """
        SELECT COUNT(DISTINCT cl.id)
        FROM dbp.call_list cl
        JOIN dbp.participant p ON cl.id = p.call_list_id
        WHERE p.user_id = :userId AND cl.call_type != 'callList'
        """
        )
            .bind("userId", userId)
            .mapTo(Int::class.java)
            .one()

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

    override fun getCallListsByCompetitionId(competitionId: Int): CallList? {
        return handle
            .createQuery(
                """select * from dbp.call_list where competition_id = :competition_id""",
            ).bind("competition_id", competitionId)
            .mapTo<CallList>()
            .singleOrNull()
    }

    override fun getCallListsFinalJuryFunction(userId: Int, callListType: String, functionId: Int): List<CallList> {
        return handle
            .createQuery(
                """
            SELECT DISTINCT cl.*
            FROM dbp.call_list cl
            INNER JOIN dbp.participant p ON cl.id = p.call_list_id
            WHERE p.user_id = :user_id
              AND p.function_id = :function_id
              AND cl.call_type = :call_list_type
            """
            )
            .bind("user_id", userId)
            .bind("function_id", functionId)
            .bind("call_list_type", callListType)
            .mapTo<CallList>()
            .list()
    }


}
