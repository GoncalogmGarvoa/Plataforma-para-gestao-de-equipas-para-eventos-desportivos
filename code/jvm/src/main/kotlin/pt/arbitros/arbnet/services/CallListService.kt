package pt.arbitros.arbnet.services

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.http.model.MatchDaySessionsInput
import pt.arbitros.arbnet.http.model.UserInputIdModel
import pt.arbitros.arbnet.repository.TransactionManager
import java.time.LocalDate

@Component
class CallListService(
    private val transactionManager: TransactionManager,
    // private val usersDomain: UsersDomain,
    // private val clock: Clock
) {
    // todo "juiz arbitro" and "delegado" not defined
    // todo where do i get councilId ?
    // todo where do i get "cargo" from participant
    // todo in call list i need to show the referee's category
    fun createCallList(
        competitionName: String,
        competitionNumber: Int,
        address: String,
        phoneNumber: Int,
        email: String,
        association: String,
        location: String,
        participant: List<UserInputIdModel>, // todo list of what ? does it make sense
        deadline: LocalDate,
        callType: String, // i still dont know what do you mean by type but ok.
        matchDaySessions: List<MatchDaySessionsInput>,
        // matchDay: List<String>,
        // session: List<String>,
    ): Int {
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

            val matchDayRepository = it.matchDayRepository
            val matchDayList = mutableListOf<Int>() // todo maybe mutableListof<MatchDay,ID>
            repeat(matchDaySessions.size) { idx ->
                val matchDayId =
                    matchDayRepository.createMatchDay(
                        competitionId,
                        matchDaySessions[idx].matchDay,
                    )
                matchDayList.add(matchDayId)
            }

            val sessionsRepository = it.sessionsRepository
            repeat(matchDaySessions.size) { matchDayIdx ->
                val sessionList = matchDaySessions[matchDayIdx].sessions
                repeat(sessionList.size) { idx ->
                    sessionsRepository.createSession(
                        competitionId,
                        matchDayList[matchDayIdx],
                        sessionList[idx],
                    )
                }
            }

            val callListRepository = it.callListRepository
            val callListId =
                callListRepository.createCallList(
                    deadline,
                    callType,
                    0, // todo
                    competitionId,
                )

            val participantRepository = it.participantRepository
            repeat(matchDaySessions.size) { matchDay ->
                repeat(participant.size) { user ->
                    // todo need to check if an user works all match_days
                    participantRepository.addParticipant(
                        callListId,
                        matchDayList[matchDay], // todo not the best way
                        0, // todo
                        competitionId,
                        participant[user].id,
                        "cargo",
                    )
                }
            }
        }
        return 0
    }
}
