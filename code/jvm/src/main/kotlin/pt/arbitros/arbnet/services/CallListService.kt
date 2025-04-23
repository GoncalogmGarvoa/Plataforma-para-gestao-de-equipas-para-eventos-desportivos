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
        deadline: LocalDate,
        councilId: Int,
        participant: List<Int>,
        matchDaySessions: List<MatchDaySessionsInput>,
    ): Int {
        transactionManager.run {
            // Create the competition
            val competitionRepository = it.competitionRepository
            val matchDayRepository = it.matchDayRepository
            val sessionsRepository = it.sessionsRepository
            val callListRepository = it.callListRepository
            val participantRepository = it.participantRepository
            val roleRepository = it.roleRepository

            // Create the competition
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

            val matchDayMap = mutableMapOf<MatchDaySessionsInput, Int>()

            matchDaySessions.forEach { matchDay ->
                val matchDayId =
                    matchDayRepository.createMatchDay(
                        competitionId,
                        matchDay.matchDay,
                    )
                matchDayMap[matchDay] = matchDayId
            }

            matchDaySessions.forEach { matchDay ->
                val matchDayId = matchDayMap[matchDay]!!
                matchDay.sessions.forEach { session ->
                    sessionsRepository.createSession(
                        competitionId,
                        matchDayId,
                        session,
                    )
                }
            }

            val callListId =
                callListRepository.createCallList(
                    deadline,
                    councilId,
                    competitionId,
                )

            val roleId = roleRepository.getRoleIdByName("default")

            matchDaySessions.forEach { matchDay ->
                val matchDayId = matchDayMap[matchDay]!!
                participant.forEach { user ->
                    participantRepository.addParticipant(
                        callListId,
                        matchDayId,
                        councilId,
                        competitionId,
                        user,
                        roleId,
                    )
                }
            }
        }
        return 0
    }

    fun assignRoles(): Boolean {
        // TODO
        return true
    }
}
