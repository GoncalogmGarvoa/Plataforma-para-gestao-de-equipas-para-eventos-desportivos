@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.*
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

    data object CompetitionNotFound : CallListError()

    data object SessionNotFound : CallListError() // todo

    data object InvalidCompetitionName : CallListError()

    data object InvalidAddress : CallListError()

    data object InvalidPhoneNumber : CallListError()

    data object InvalidEmail : CallListError()

    data object InvalidAssociation : CallListError()

    data object InvalidLocation : CallListError()

    data object InvalidCallListType : CallListError() // todo
}

@Component
class CallListService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val utilsDomain: UtilsDomain,
    private val callListDomain: CallListDomain,
    // private val clock: Clock
) {
    // todo create rollback
    // todo Event > callList + competition
    fun createEvent(callList: CallListInputModel): Either<CallListError, Int> =
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

            val callListId =
                createCallListOnly(
                    callList,
                    it.callListRepository,
                    competitionId,
                ) ?: return@run failure(CallListError.CallListNotFound)

            if (callList.participants?.isEmpty() == true) {
                val participantsResult =
                    createParticipantsOnly(
                        callList,
                        matchDayMap,
                        callListId,
                        competitionId,
                        it.functionRepository,
                        it.participantRepository,
                    )
                if (participantsResult is Failure) return@run participantsResult
            }

            success(callListId)
        }

    private fun validateAndCheckUsers(
        callList: CallListInputModel,
        usersRepository: UsersRepository,
    ): Either<CallListError, List<Users>>? {
        val validateResult =
            validateCallList(
                callList.competitionName,
                callList.address,
                callList.phoneNumber,
                callList.email,
                callList.association,
                callList.location,
                callList.callListType,
            )
        if (validateResult is Failure) return validateResult

        if (!usersRepository.userHasCouncilRole(callList.userId)) {
            return failure(CallListError.ArbitrationCouncilNotFound)
        }
        if (callList.participants?.isEmpty() == true) { // todo change to safe call,    only possible in callList draft needs to check
            return success(emptyList())
        } else {
            val participantIds = callList.participants?.map { it.userId }
            val foundReferees = usersRepository.getUsersAndCheckIfReferee(participantIds!!) // todo change !!
            if (foundReferees.size != callList.participants.size) {
                return failure(CallListError.ParticipantNotFound)
            }

            return success(foundReferees)
        }
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
            callList
                .matchDaySessions
                .associate { md ->
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

    private fun createCallListOnly(
        callList: CallListInputModel,
        callListRepository: CallListRepository,
        competitionId: Int,
    ): Int =
        callListRepository.createCallList(
            callList.deadline,
            callList.userId,
            competitionId,
            callList.callListType,
        )

    private fun createParticipantsOnly(
        callList: CallListInputModel,
        matchDayMap: Map<LocalDate, Int>,
        callListId: Int,
        competitionId: Int,
        functionRepository: FunctionRepository,
        participantRepository: ParticipantRepository,
    ): Either<CallListError, Unit> {
        val participantsToInsert = mutableListOf<Participant>()

        for (p in callList.participants!!) { // todo change to safe call
            for ((day, funcName) in p.participantAndRole) {
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
        return success(Unit)
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

    fun getParticipantsByCallList(callListId: Int): Either<CallListError, List<Participant>> =
        transactionManager.run {
            val participantRepository = it.participantRepository
            val callListRepository = it.callListRepository

            // Check if the call list exists
            callListRepository.getCallListById(callListId)
                ?: return@run failure(CallListError.CallListNotFound)

            val participants = participantRepository.getParticipantsByCallList(callListId)
            return@run success(participants)
        }

    fun getCompetitionByCallList(callListId: Int): Either<CallListError, Competition> =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val competitionRepository = it.competitionRepository

            // Check if the call list exists
            val callList =
                callListRepository.getCallListById(callListId)
                    ?: return@run failure(CallListError.CallListNotFound)

            val competition =
                competitionRepository.getCompetitionById(callList.competitionId)
                    ?: return@run failure(CallListError.CallListNotFound)

            return@run success(competition)
        }

    fun getMatchDaysByCallList(callListId: Int): Either<CallListError, List<MatchDay>> =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val matchDayRepository = it.matchDayRepository

            // Check if the call list exists
            val callList =
                callListRepository.getCallListById(callListId)
                    ?: return@run failure(CallListError.CallListNotFound)

            val matchDays =
                matchDayRepository.getMatchDaysByCompetition(callList.competitionId)
                    ?: return@run failure(CallListError.MatchDayNotFound)

            return@run success(matchDays)
        }

    fun getSessionByMatchDay(matchDayId: Int): Either<CallListError, List<Session>> =
        transactionManager.run {
            val matchDayRepository = it.matchDayRepository
            val sessionsRepository = it.sessionsRepository

            // Check if the call list exists
            val matchDay =
                matchDayRepository.getMatchDayById(matchDayId)
                    ?: return@run failure(CallListError.CallListNotFound)

            val session =
                sessionsRepository.getSessionByMatchId(matchDay.id)
                    ?: return@run failure(CallListError.SessionNotFound)
            return@run success(session)
        }

    fun getEventById(id: Int): Either<CallListError, Event> =
        transactionManager.run {
            val callListRepository = it.callListRepository
            val competitionRepository = it.competitionRepository
            val participantRepository = it.participantRepository
            val matchDayRepository = it.matchDayRepository
            val sessionsRepository = it.sessionsRepository

            val callList: CallList =
                callListRepository.getCallListById(id)
                    ?: return@run failure(CallListError.CallListNotFound)

            val competition =
                competitionRepository.getCompetitionById(callList.competitionId)
                    ?: return@run failure(CallListError.CallListNotFound)

            val participants = participantRepository.getParticipantsByCallList(id)

            val matchDays: List<MatchDay> =
                matchDayRepository.getMatchDaysByCompetition(callList.competitionId)
                    ?: return@run failure(CallListError.MatchDayNotFound)

            val event =
                Event(
                    competitionName = competition.name,
                    address = competition.address,
                    phoneNumber = competition.phoneNumber,
                    email = competition.email,
                    association = competition.association,
                    location = competition.location,
                    userId = callList.userId,
                    participants = participants,
                    deadline = callList.deadline,
                    callListType = callList.callType,
                    matchDaySessions = matchDays,
                )

            return@run success(event)
        }

    private fun validateCallList(
        competitionName: String,
        address: String,
        phoneNumber: String,
        email: String,
        association: String,
        location: String,
        callType: String,
    ): Either<CallListError, Unit> {
        if (!callListDomain.validCallListType(callType)) return failure(CallListError.InvalidCallListType)
        if (!utilsDomain.validName(competitionName)) return failure(CallListError.InvalidCompetitionName)
        if (!utilsDomain.validAddress(address)) return failure(CallListError.InvalidAddress)
        if (!utilsDomain.validPhoneNumber(phoneNumber)) return failure(CallListError.InvalidPhoneNumber)
        if (!utilsDomain.validEmail(email)) return failure(CallListError.InvalidEmail)
        if (!utilsDomain.validName(association)) return failure(CallListError.InvalidAssociation)
        if (!utilsDomain.validName(location)) return failure(CallListError.InvalidLocation)

        return success(Unit)
    }
}
