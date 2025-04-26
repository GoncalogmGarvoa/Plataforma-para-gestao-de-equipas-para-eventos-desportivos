package pt.arbitros.arbnet.services

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.http.model.MatchDaySessionsInput
import pt.arbitros.arbnet.http.model.RoleAssignmentsInput
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
        val callList =
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
                callListId
            }
        return callList
    }

    fun assignRoles(roleAssignmentsInfo: List<RoleAssignmentsInput>): Boolean {
        transactionManager.run {
            val roleRepository = it.roleRepository
            val participantRepository = it.participantRepository

            roleAssignmentsInfo.forEach { roleAssignment ->
                val roleId = roleRepository.getRoleIdByName(roleAssignment.role)
                roleAssignment.assignments.forEach { assignment ->
                    val sucess =
                        participantRepository.updateParticipantRole(
                            assignment.participantId,
                            roleId,
                            assignment.matchDayId,
                        )
                }
            }
        }
        return true
    }

    fun updateParticipantConfirmationStatus(
        days: List<Int>,
        participantId: Int,
        callListId : Int,
    ): Boolean {
        transactionManager.run {
            val participantRepository = it.participantRepository
            participantRepository.updateParticipantConfirmationStatus(days, participantId, callListId)
        }
        return true
    }
}
