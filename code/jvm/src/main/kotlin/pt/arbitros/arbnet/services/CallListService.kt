package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ConfirmationStatus
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.FunctionsAssignmentsInput
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo
import java.time.LocalDate

sealed class CallListError {
    data object ArbitrationCouncilNotFound : CallListError()

    data object FunctionNotFound : CallListError()

    data object ParticipantNotFound : CallListError()

    data object CallListNotFound : CallListError()

    data object MatchDayNotFound : CallListError()
}

@Component
class CallListService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    // private val usersDomain: UsersDomain,
    // private val clock: Clock
) {
    // todo create rollback
    fun createCallList(callList: CallListInputModel): Either<CallListError, Int> =
        transactionManager.run { it ->
            // Create the competition
            val competitionRepository = it.competitionRepository
            val matchDayRepository = it.matchDayRepository
            val sessionsRepository = it.sessionsRepository
            val callListRepository = it.callListRepository
            val participantRepository = it.participantRepository
            val functionRepository = it.functionRepository
            val usersRepository = it.usersRepository

            // Check if the council exists
            if (!usersRepository.userHasCouncilRole(callList.userId)) {
                return@run failure(CallListError.ArbitrationCouncilNotFound)
            }

            // Check if the participants exist

            val participantIds = callList.participants.map { it.userId }

            val foundReferees = usersRepository.getUsersAndCheckIfReferee(participantIds)
            if (foundReferees.size != callList.participants.size) {
                return@run failure(CallListError.ParticipantNotFound)
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
            val matchDayMap: Map<LocalDate, Int> =
                callList.matchDaySessions.associate { md ->
                    md.matchDay to matchDayRepository.createMatchDay(competitionId, md.matchDay)
                }
            callList.matchDaySessions.forEach { md ->
                val mdId = matchDayMap[md.matchDay]!!
                md.sessions.forEach { tm ->
                    sessionsRepository.createSession(competitionId, mdId, tm)
                }
            }
            val callListId =
                callListRepository.createCallList(
                    callList.deadline,
                    callList.userId,
                    competitionId,
                )

            val participantsToInsert = mutableListOf<Participant>()

            callList.participants.forEach { p ->
                p.functionByMatchDay
                    .filter { elem -> elem.function.isNotBlank() }
                    .forEach { (day, funcName) ->
                        val funcId =
                            functionRepository.getFunctionIdByName(funcName)
                                ?: return@run failure(CallListError.FunctionNotFound)
                        val mdId =
                            matchDayMap[day]
                                ?: return@run failure(CallListError.MatchDayNotFound)
                        participantsToInsert +=
                            Participant(
                                callListId = callListId,
                                matchDayId = mdId,
                                competitionIdMatchDay = competitionId,
                                userId = p.userId,
                                functionId = funcId,
                                confirmationStatus = ConfirmationStatus.WAITING.value,
                            )
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
