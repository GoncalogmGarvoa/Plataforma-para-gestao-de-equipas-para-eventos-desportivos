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
    // todo "juiz arbitro" and "delegado" not defined / R: fazer depois pois nao sabemos quem aceitou
    // todo where do i get councilId ? /R: tem de vir na criação da convocatoria
    // todo where do i get "cargo" from participant /R: startam todos a default
    fun createCallList(
        competitionName: String,
        competitionNumber: Int,
        address: String,
        phoneNumber: Int,
        email: String,
        association: String,
        location: String,
        deadline: LocalDate,
        councilId: Int,
        participant: List<Int>, // todo list of what ? does it make sense /R: definitivamente nao e este objeto na lista
        matchDaySessions: List<MatchDaySessionsInput>,
    ): Int {
        transactionManager.run {
            // Create the competition
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

            // Create the match day sessions
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
                    "convocatoria",
                    councilId,
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
                        participant[user],
                        "cargo",
                    )
                }
            }
        }
        return 0
    }
}
