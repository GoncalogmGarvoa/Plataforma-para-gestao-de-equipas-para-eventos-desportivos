@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ConfirmationStatus
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.FunctionsAssignmentsInput
import pt.arbitros.arbnet.repository.*
import pt.arbitros.arbnet.transactionRepo
import java.time.LocalDate

sealed class CallListError {
    data object ArbitrationCouncilNotFound : CallListError()

    data object FunctionNotFound : CallListError()

    data object ParticipantNotFound : CallListError()

    data object CallListNotFound : CallListError()

    data object MatchDayNotFound : CallListError()

    data object InvalidCompetitionName : CallListError()

    data object InvalidAddress : CallListError()

    data object InvalidPhoneNumber : CallListError()

    data object InvalidEmail : CallListError()

    data object InvalidAssociation : CallListError()

    data object InvalidLocation : CallListError()
}

@Component
class CallListService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val utilsDomain: UtilsDomain,
    // private val clock: Clock
) {
    // todo create rollback
    fun createCallList(callList: CallListInputModel): Either<CallListError, Int> =
        transactionManager.run {
            val result = validateAndCheckUsers(callList, it.usersRepository)
            if (result is Failure) return@run result

            val (competitionId, matchDayMap) =
                createCompetitionAndSessions(
                    callList,
                    it.competitionRepository,
                    it.matchDayRepository,
                    it.sessionsRepository,
                )

            createCallListAndParticipants(
                callList,
                matchDayMap,
                it.functionRepository,
                it.callListRepository,
                it.participantRepository,
                competitionId,
            )
        }

    private fun validateAndCheckUsers(
        callList: CallListInputModel,
        usersRepository: UsersRepository,
    ): Either<CallListError, List<Users>> {
        val validateResult =
            validateCallList(
                callList.competitionName,
                callList.address,
                callList.phoneNumber,
                callList.email,
                callList.association,
                callList.location,
            )
        if (validateResult is Failure) return validateResult

        if (!usersRepository.userHasCouncilRole(callList.userId)) {
            return failure(CallListError.ArbitrationCouncilNotFound)
        }

        val participantIds = callList.participants.map { it.userId }
        val foundReferees = usersRepository.getUsersAndCheckIfReferee(participantIds)
        if (foundReferees.size != callList.participants.size) {
            return failure(CallListError.ParticipantNotFound)
        }

        return success(foundReferees)
    }

    private fun createCompetitionAndSessions(
        callList: CallListInputModel,
        competitionRepository: CompetitionRepository,
        matchDayRepository: MatchDayRepository,
        sessionsRepository: SessionsRepository,
    ): Pair<Int, Map<LocalDate, Int>> {
        val competitionId =
            competitionRepository.createCompetition(
                callList.competitionName,
                callList.address,
                callList.phoneNumber,
                callList.email,
                callList.association,
                callList.location,
            )

        val matchDayMap =
            callList.matchDaySessions.associate { md ->
                md.matchDay to matchDayRepository.createMatchDay(competitionId, md.matchDay)
            }

        callList.matchDaySessions.forEach { md ->
            val mdId = matchDayMap[md.matchDay]!!
            md.sessions.forEach { tm ->
                sessionsRepository.createSession(competitionId, mdId, tm)
            }
        }

        return competitionId to matchDayMap
    }

    private fun createCallListAndParticipants(
        callList: CallListInputModel,
        matchDayMap: Map<LocalDate, Int>,
        functionRepository: FunctionRepository,
        callListRepository: CallListRepository,
        participantRepository: ParticipantRepository,
        competitionId: Int,
    ): Either<CallListError, Int> {
        val callListId =
            callListRepository.createCallList(
                callList.deadline,
                callList.userId,
                competitionId,
            )

        val participantsToInsert = mutableListOf<Participant>()

        for (p in callList.participants) {
            for ((day, funcName) in p.functionByMatchDay) {
                if (funcName.isBlank()) continue

                val funcId =
                    functionRepository.getFunctionIdByName(funcName)
                        ?: return failure(CallListError.FunctionNotFound)

                val mdId =
                    matchDayMap[day]
                        ?: return failure(CallListError.MatchDayNotFound)

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

        participantRepository.batchAddParticipants(participantsToInsert)
        return success(callListId)
    }

    // Not in use for now
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

    private fun validateCallList(
        competitionName: String,
        address: String,
        phoneNumber: String,
        email: String,
        association: String,
        location: String,
    ): Either<CallListError, Unit> {
        if (!utilsDomain.validName(competitionName)) return failure(CallListError.InvalidCompetitionName)
        if (!utilsDomain.validAddress(address)) return failure(CallListError.InvalidAddress)
        if (!utilsDomain.validPhoneNumber(phoneNumber)) return failure(CallListError.InvalidPhoneNumber)
        if (!utilsDomain.validEmail(email)) return failure(CallListError.InvalidEmail)
        if (!utilsDomain.validName(association)) return failure(CallListError.InvalidAssociation)
        if (!utilsDomain.validName(location)) return failure(CallListError.InvalidLocation)

        return success(Unit)
    }
}
