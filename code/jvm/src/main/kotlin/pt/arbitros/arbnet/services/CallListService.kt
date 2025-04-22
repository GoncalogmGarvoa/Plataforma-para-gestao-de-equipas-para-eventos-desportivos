package pt.arbitros.arbnet.services

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.http.model.MatchDaySessionsInput
import pt.arbitros.arbnet.repository.TransactionManager
import java.time.LocalDate

@Component
class CallListService(
    private val transactionManager: TransactionManager,
    // private val usersDomain: UsersDomain,
    // private val clock: Clock
) {
    fun createCallList(
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
            val matchDayList = mutableListOf<Int>()
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
                repeat(matchDaySessions[matchDayIdx].sessions.size) { idx ->

                    sessionsRepository.createSession(
                        competitionId,
                        matchDayList[matchDayIdx],
                        session[idx],
                    )
                }
            }

            val callListRepository = it.callListRepository
            val callListId =
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

            val refereeRepository = it.refereeRepository
            val refereesList =
                refereeRepository.getReferees(
                    participant,
                )

            val participantRepository = it.participantRepository
            val participant =
                participantRepository.createParticipant(
                    matchDayId, // todo List ?
                    competitionId,
                    callListId,
                    referees,
                )
        }
        return 0
    }
}
