package pt.arbitros.arbnet.services

// <<<<<<< Updated upstream
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.domain.Referee
import pt.arbitros.arbnet.repository.TransactionManager
import java.time.LocalDate

@Component
class CallListService(
    private val transactionManager: TransactionManager,
    // private val usersDomain: UsersDomain,
    // private val clock: Clock
) {
/*fun createCallList(
    competitionName: String,
    competitionNumber: Int,
    address: String,
    phoneNumber: Int,
    email: String,
    association: String,
    location: String,
    participant: List<String>, // todo list of String or list of Particpant
    timeLine: LocalDate,
    type: String, // i still dont know what do you mean by type but ok.
    matchDay: List<String>,
    session: List<String>,
): Int {
    val competitionId =
        transactionManager.run {
            val competitionRepository = it.competitionRepository
            val competitionId =
                competitionRepository.createCompetition(
                    competitionName,
                    competitionNumber,
                    address,
                    phoneNumber,
                    email,
                    association,
                    location,
                )
            competitionId
        }

    // todo if there is 3 matchDays i need to make 3 creates and save the 3 id's of them
    // maybe the return is a list of ids or matchDays
    val matchDayId =
        transactionManager.run {
            val matchDayRepository = it.matchDayRepository
            val matchDayId =
                matchDayRepository.createMatchDay(
                    competitionId,
                    matchDay,
                )
        }

    // todo same thing that happens in match days in here
    // maybe the return is a list of ids or sessions

    val sessionsId =
        transactionManager.run {
            val sessionsRepository = it.sessionsRepository
            val sessionId =
                sessionsRepository.createSessions(
                    matchDayId,
                    competitionId,
                    session,
                )
        }

    val callListId =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val id =
                callListRepository.createCallList(
                    competitionName,
                    competitionNumber,
                    address,
                    phoneNumber,
                    email,
                    association,
                    location,
                    participant,
                    timeLine,
                    type,
                )
            id
        }

    val referees =
        transactionManager.run {
            val refereeRepository = it.refereeRepository
            val refereesList =
                refereeRepository.getReferees(
                    participant,
                )
            refereesList
        }

    val participant =
        transactionManager.run {
            val participantRepository = it.participantRepository
            val participant =
                participantRepository.createParticipant(
                    matchDayId, // todo List ?
                    competitionId,
                    callListId,
                    referees,
                )
        }
    return callListId
}


class CallListService(
    private val transactionManager: TransactionManager,
    private val callListDomain: CallListDomain,
// private val clock: Clock
) {*/
    fun createCallList(
        deadline: LocalDate,
        callType: String,
        councilId: Int,
        competitionId: Int,
    ): Int =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val callListId =
                callListRepository.createCallList(
                    deadline,
                    callType,
                    councilId,
                    competitionId,
                )
            callListId
        }

    fun getCallListById(id: Int): CallList =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val callList =
                callListRepository.findCallListById(id)
                    ?: throw Exception("Call list with id $id not found")
            callList
        }

    fun getCallListsByCouncil(councilId: Int): List<CallList> =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val callLists = callListRepository.getCallListsByCouncil(councilId)
            callLists
        }

    fun deleteCallList(id: Int): Boolean =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val deleted = callListRepository.deleteCallList(id)
            deleted
        }

    fun updateCallList(
        id: Int,
        deadline: LocalDate,
        callType: String,
        councilId: Int,
        competitionId: Int,
    ): Boolean =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val updated =
                callListRepository.updateCallList(
                    id,
                    deadline,
                    callType,
                    councilId,
                    competitionId,
                )
            updated
        }

    fun getCallListReferees(callListId: Int): List<Referee> =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val referees = callListRepository.getCallListReferees(callListId)
            referees
        }
// >>>>>>> Stashed changes
}
