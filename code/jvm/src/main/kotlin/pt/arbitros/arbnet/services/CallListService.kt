package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ConfirmationStatus
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.http.model.MatchDaySessionsInput
import pt.arbitros.arbnet.http.model.RoleAssignmentsInput
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo
import java.time.LocalDate

@Component
class CallListService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
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
        participants: List<Int>,
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
                //val usersRepository = it.usersRepository
                val refereeRepository = it.refereeRepository
                val arbitrationCouncilRepository = it.arbitrationCouncilRepository

                // Check if the council exists
                arbitrationCouncilRepository.getCouncilMemberById(councilId)
                    ?: throw Exception("Council with id $councilId not found")

                // Check if the participants exist
                val foundReferees = refereeRepository.getAllReferees(participants)
                if (foundReferees.size != participants.size) {
                    throw Exception("One or more of the participants were not found")
                }

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

                val participantsToInsert = mutableListOf<Participant>()

                for (matchDay in matchDaySessions) {
                    val matchDayId = matchDayMap[matchDay]!!
                    for (userId in participants) {
                        val participant = Participant(
                            callListId = callListId,
                            matchDayId = matchDayId,
                            //councilId = councilId,
                            competitionIdMatchDay = competitionId,
                            refereeId = userId,
                            0,
                            ConfirmationStatus.WAITING,
                        )
                        participantsToInsert.add(participant)
                    }
                }

                participantRepository.batchAddParticipants(participantsToInsert.toList())


                callListId
            }
        return callList
    }

    fun assignRoles(roleAssignmentsInfo: List<RoleAssignmentsInput>): Boolean {
        transactionManager.run {
            val roleRepository = it.roleRepository
            val participantRepository = it.participantRepository
            val matchDayRepository = it.matchDayRepository

            roleAssignmentsInfo.forEach { roleAssignment ->

                val roleId = roleRepository.getRoleIdByName(roleAssignment.role)
                    ?: throw Exception("Role with name ${roleAssignment.role} not found")
                roleAssignment.assignments.forEach { assignment ->
                    //Check if the participant exists
                    participantRepository.getParticipantById(assignment.participantId)
                        ?: throw Exception("Participant with id ${assignment.participantId} not found")

                    //Check if the match day exists
                    matchDayRepository.getMatchDayById(assignment.matchDayId)
                        ?: throw Exception("Match day with id ${assignment.matchDayId} not found")

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
        callListId: Int,
    ): Boolean {
        transactionManager.run {
            val participantRepository = it.participantRepository
            val callListRepository = it.callListRepository

            // Check if the participant exists
            participantRepository.getParticipantById(participantId)
                ?: throw Exception("Participant with id $participantId not found")

            // Check if the call list exists
            callListRepository.getCallListById(callListId)
                ?: throw Exception("Call list with id $callListId not found")

            participantRepository.updateParticipantConfirmationStatus(days, participantId, callListId)
            if (participantRepository.isCallListDone(callListId)) {
                callListRepository.updateCallListStatus(callListId)
            }
        }
        return true
    }
}
