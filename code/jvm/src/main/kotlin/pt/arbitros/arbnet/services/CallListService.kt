package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ConfirmationStatus
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.MatchDaySessionsInput
import pt.arbitros.arbnet.http.model.FunctionsAssignmentsInput
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo

sealed class CallListError {
    data object ArbitrationCouncilNotFound : CallListError()

    data object FunctionNotFound : CallListError()

    data object ParticipantNotFound : CallListError()

    data object CallListNotFound : CallListError()

    data object MatchDayNotFound : CallListError()

    data object ParticipantsDontMatchFunctions : CallListError()
}

@Component
class CallListService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    // private val usersDomain: UsersDomain,
    // private val clock: Clock
) {
    fun createCallList(
        callList : CallListInputModel,
    ): Either<CallListError, Int> =
        transactionManager.run {
            // Create the competition
            val competitionRepository = it.competitionRepository
            val matchDayRepository = it.matchDayRepository
            val sessionsRepository = it.sessionsRepository
            val callListRepository = it.callListRepository
            val participantRepository = it.participantRepository
            val functionRepository = it.functionRepository
            val usersRepository = it.usersRepository

            // Check if the council exists
            if (!usersRepository.userHasCouncilRole(callList.councilId)) {
                return@run failure(CallListError.ArbitrationCouncilNotFound)
            }

            // Check if the participants exist
            val foundReferees = usersRepository.getUsersAndCheckIfReferee(callList.participants)

            if (foundReferees.size != callList.participants.size) {
                return@run failure(CallListError.ParticipantNotFound)
            }

            // Check if the functions exist
            val foundFunctions = functionRepository.getFunctionIds(callList.functions)
            if(foundFunctions.size != callList.functions.size) {
                return@run failure(CallListError.FunctionNotFound)
            }

            // Check if participants and functions have the same size
            if (foundReferees.size != foundFunctions.size) {
                return@run failure(CallListError.ParticipantsDontMatchFunctions)
            }

            // Create the competition
            val competitionId =
                competitionRepository.createCompetition(
                    callList.competitionName,
                    callList.address,
                    callList.phoneNumber,
                    callList.email,
                    callList.association,
                    callList.location,
                )

            // Create the match day sessions

            val matchDayMap = mutableMapOf<MatchDaySessionsInput, Int>()

            callList.matchDaySessions.forEach { matchDay ->
                val matchDayId =
                    matchDayRepository.createMatchDay(
                        competitionId,
                        matchDay.matchDay,
                    )
                matchDayMap[matchDay] = matchDayId
            }

            callList.matchDaySessions.forEach { matchDay ->
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
                    callList.deadline,
                    callList.councilId,
                    competitionId,
                )

            val participantsToInsert = mutableListOf<Participant>()

            for (matchDay in callList.matchDaySessions) {
                val matchDayId = matchDayMap[matchDay]!!
                for (userId in 0..callList.participants.size-1) {
                    val participant =
                            Participant(
                                callListId = callListId,
                                matchDayId = matchDayId,
                                // councilId = councilId,
                                competitionIdMatchDay = competitionId,
                                userId = foundReferees[userId].id,
                                foundFunctions[userId],
                                ConfirmationStatus.WAITING.value,
                            )
                        participantsToInsert.add(participant)
                }
            }
            participantRepository.batchAddParticipants(participantsToInsert.toList())

            return@run success(callListId)
        }

    fun assignFunction(functionAssignmentsInfo: List<FunctionsAssignmentsInput>): Either<CallListError, Boolean> =
        transactionManager.run {
            val functionRepository = it.functionRepository
            val participantRepository = it.participantRepository
            val matchDayRepository = it.matchDayRepository

            functionAssignmentsInfo.forEach { functionAssignment ->

                val functionId =
                    functionRepository.getFunctionIdByName(functionAssignment.function)
                        ?: return@run failure(CallListError.FunctionNotFound)
                functionAssignment.assignments.forEach { assignment ->
                    // Check if the participant exists
                    participantRepository.getParticipantById(assignment.participantId)
                        ?: return@run failure(CallListError.ParticipantNotFound)

                    // Check if the match day exists
                    matchDayRepository.getMatchDayById(assignment.matchDayId)
                        ?: return@run failure(CallListError.MatchDayNotFound)

                    participantRepository.updateParticipantRole(
                        assignment.participantId,
                        functionId,
                        assignment.matchDayId,
                    )
                }
            }
            return@run success(true) // todo check
        }

    fun updateParticipantConfirmationStatus(
        days: List<Int>,
        participantId: Int,
        callListId: Int,
    ): Either<CallListError, Boolean> =
        transactionManager.run {
            val participantRepository = it.participantRepository
            val callListRepository = it.callListRepository

            // Check if the participant exists
            participantRepository.getParticipantById(participantId)
                ?: return@run failure(CallListError.ParticipantNotFound)

            // Check if the call list exists
            callListRepository.getCallListById(callListId)
                ?: return@run failure(CallListError.CallListNotFound)

            participantRepository.updateParticipantConfirmationStatus(days, participantId, callListId)
            if (participantRepository.isCallListDone(callListId)) {
                callListRepository.updateCallListStatus(callListId)
            }
            return@run success(true)
        }
}
