@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.*
import pt.arbitros.arbnet.domain.users.Users
import pt.arbitros.arbnet.http.model.CallListInputLike
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.CallListInputUpdateModel
import pt.arbitros.arbnet.http.model.EventOutputModel
import pt.arbitros.arbnet.http.model.ParticipantChoice
import pt.arbitros.arbnet.http.model.ParticipantWithCategory
import pt.arbitros.arbnet.repository.*
import pt.arbitros.arbnet.transactionRepo
import java.time.LocalDate

sealed class CallListError {
    data object ArbitrationCouncilNotFound : CallListError()

    data object ParticipantDoesntHaveACategory : CallListError()

    data object CategoryNotFound : CallListError()

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
    private val callListMongoRepository: CallListMongoRepository,
    private val utilsDomain: UtilsDomain,
    private val callListDomain: CallListDomain,
    // private val clock: Clock
) {

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
                ) ?: return@run failure(CallListError.CallListNotFound) // todo verify

            if (callList.participants?.isNotEmpty() == true) {
                val participantsResult =
                    createParticipantsOnly(
                        callList.participants,
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

    fun updateEvent(callList: CallListInputUpdateModel): Either<CallListError, Int> =
        transactionManager.run {
            // check if callList with id exists
            it.callListRepository.getCallListById(callList.callListId)
                ?: return@run failure(CallListError.CallListNotFound)

            val result = validateAndCheckUsers(callList, it.usersRepository)
            if (result is Failure) return@run result

            val (competitionId, matchDayMap) =
                updateCompetitionAndSessions(
                    callList,
                    it.competitionRepository,
                    it.matchDayRepository,
                    it.sessionsRepository,
                )

            val callListId =
                updateCallListOnly(
                    callList,
                    it.callListRepository,
                    competitionId,
                ) ?: return@run failure(CallListError.CallListNotFound) //TODO review

            if (callList.participants?.isNotEmpty() == true) {
                val participantsResult =
                    createParticipantsOnly(
                        callList.participants,
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
        callList: CallListInputLike,
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

        usersRepository.getUserById(callList.userId)
            ?: return failure(CallListError.ArbitrationCouncilNotFound)

         if(usersRepository.userHasCouncilRole(callList.userId))
             return failure(CallListError.ArbitrationCouncilNotFound)


        val participants = callList.participants
        if (participants.isNullOrEmpty()) {
            return success(emptyList<Users>())
        }

        val participantIds = participants.map { it.userId }
        val foundReferees = usersRepository.getUsersAndCheckIfReferee(participantIds)

        if (foundReferees.size != participants.size) {
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

    fun updateCompetitionAndSessions(
        callList: CallListInputUpdateModel,
        competitionRepository: CompetitionRepository,
        matchDayRepository: MatchDayRepository,
        sessionsRepository: SessionsRepository,
    ): Pair<Int, Map<LocalDate, Int>> {
        val competitionId =
            competitionRepository.updateCompetition(
                callList.callListId,
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
                    val matchDayId =
                        matchDayRepository.getMatchDayId(competitionId, md.matchDay)
                            ?: throw IllegalArgumentException("Match day not found") // TODO ERROR HANDLING
                    md.matchDay to matchDayRepository.updateMatchDay(matchDayId, competitionId, md.matchDay)
                }

        callList.matchDaySessions.forEach { md ->
            val mdId = matchDayMap[md.matchDay]!!
            md.sessions.forEach { tm ->
                val sessions = sessionsRepository.getSessionByMatchId(mdId)
                sessions.forEach {
                    sessionsRepository.updateSession(it.id, competitionId, mdId, tm)
                }
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

    fun updateCallListOnly(
        callList: CallListInputUpdateModel,
        callListRepository: CallListRepository,
        competitionId: Int,
    ): Int =
        callListRepository.updateCallList(
            callList.callListId,
            callList.deadline,
            callList.callListType,
            competitionId,
        )

    private fun createParticipantsOnly(
        participants: List<ParticipantChoice>?,
        matchDayMap: Map<LocalDate, Int>,
        callListId: Int,
        competitionId: Int,
        functionRepository: FunctionRepository,
        participantRepository: ParticipantRepository,
    ): Either<CallListError, Unit> {
        val participantsToInsert = mutableListOf<Participant>()

        if (participants != null) {
            for (p in participants) {
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
        }

        return success(Unit)
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

    fun getParticipantsByCallList(
        tx: Transaction,
        callList: CallList,
    ): Either<CallListError, List<Participant>> {
        val participantRepository = tx.participantRepository

        val participants = participantRepository.getParticipantsByCallList(callList.id)
        return success(participants)
    }

    fun getCompetitionById(
        tx: Transaction,
        competitionId: Int,
    ): Either<CallListError, Competition> {
        val competitionRepository = tx.competitionRepository

        val competition =
            competitionRepository.getCompetitionById(competitionId)
                ?: return failure(CallListError.CompetitionNotFound)

        return success(competition)
    }

    fun getMatchDaysByCompetitionId(
        tx: Transaction,
        competitionId: Int,
    ): Either<CallListError, List<MatchDay>> {
        val matchDayRepository = tx.matchDayRepository

        val matchDays =
            matchDayRepository.getMatchDaysByCompetition(competitionId)
                ?: return failure(CallListError.MatchDayNotFound)

        return success(matchDays)
    }

    fun getEventById(id: Int): Either<CallListError, EventOutputModel> =
        transactionManager.run { tx ->
            val callListRepository = tx.callListRepository

            val callList =
                callListRepository.getCallListById(id)
                    ?: return@run failure(CallListError.CallListNotFound)

            val competitionResult = getCompetitionById(tx, callList.competitionId)
            val participantsResult = getParticipantsByCallList(tx, callList)
            val matchDaysResult = getMatchDaysByCompetitionId(tx, callList.competitionId)

            when {
                competitionResult is Failure -> return@run failure(competitionResult.value)
                participantsResult is Failure -> return@run failure(participantsResult.value)
                matchDaysResult is Failure -> return@run failure(matchDaysResult.value)
            }

            val competition = (competitionResult as Success).value
            val participants = (participantsResult as Success).value
            val matchDays = (matchDaysResult as Success).value

            val participantsWithCategory = participants.map{
                val categoryId = tx.categoryDirRepository.getCategoryIdByUserId(it.userId)
                    ?: return@run failure(CallListError.ParticipantDoesntHaveACategory)
                val category = tx.categoryRepository.getCategoryNameById(categoryId)
                    ?: return@run failure(CallListError.CategoryNotFound)

                ParticipantWithCategory(
                    callListId = it.callListId,
                    matchDayId = it.matchDayId,
                    competitionIdMatchDay = it.competitionIdMatchDay,
                    userId = it.userId,
                    functionId = it.functionId,
                    confirmationStatus = it.confirmationStatus,
                    category = category,
                )
            }

            val event =
                EventOutputModel(
                    competitionName = competition.name,
                    address = competition.address,
                    phoneNumber = competition.phoneNumber,
                    email = competition.email,
                    association = competition.association,
                    location = competition.location,
                    userId = callList.userId,
                    participants = participantsWithCategory,
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

fun updateCallListStage(callListId: Int): Either<CallListError, Boolean> =
    transactionManager.run { tx ->
        val callListRepository = tx.callListRepository

        val callList =
            callListRepository.getCallListById(callListId)
                ?: return@run failure(CallListError.CallListNotFound)

        val participants = tx.participantRepository.getParticipantsByCallList(callListId)

        if (participants.isEmpty()) {
            return@run failure(CallListError.ParticipantNotFound)
        }


        val callType =
            when (callList.callType) {
                CallListType.CALL_LIST.callType -> {
                    CallListType.SEALED_CALL_LIST.callType
                }
                CallListType.CONFIRMATION.callType -> {
                    CallListType.FINAL_JURY.callType
                }
                else -> return@run failure(CallListError.InvalidCallListType)
            }

        callListRepository.updateCallListStage(callListId, callType)

        //TODO code below is repeated from above put all in one function
        val callListContent = callListRepository.getCallListById(callListId)!!

        val competitionInfo = tx.competitionRepository.getCompetitionById(callListContent.competitionId)
            ?: return@run failure(CallListError.CompetitionNotFound)

        val participantsWithCategory = participants.map{
            val categoryId = tx.categoryDirRepository.getCategoryIdByUserId(it.userId)
                ?: return@run failure(CallListError.ParticipantDoesntHaveACategory)
            val category = tx.categoryRepository.getCategoryNameById(categoryId)
                ?: return@run failure(CallListError.CategoryNotFound)

            ParticipantWithCategory(
                callListId = it.callListId,
                matchDayId = it.matchDayId,
                competitionIdMatchDay = it.competitionIdMatchDay,
                userId = it.userId,
                functionId = it.functionId,
                confirmationStatus = it.confirmationStatus,
                category = category,
            )
        }

        val matchDays = tx.matchDayRepository.getMatchDaysByCompetition(callListContent.competitionId)

        val callListMongo = populateCallListMongo(
            competition = competitionInfo,
            callList = callListContent,
            matchDays = matchDays,
            participants = participantsWithCategory,
        )

        callListMongoRepository.save(callListMongo)

        return@run success(true)
    }


    fun populateCallListMongo(
        competition: Competition,
        callList: CallList,
        matchDays: List<MatchDay>,
        participants: List<ParticipantWithCategory>,
    ): CallListDocument {

        val competitionInfo = CompetitionInfo(
            competitionNumber = competition.competitionNumber,
            name = competition.name,
            location = competition.location,
            address = competition.address,
            association = competition.association,
            email = competition.email,
            phoneNumber = competition.phoneNumber,
        )

        val matchDayEntries = matchDays.map { matchDay ->
            val sessionEntries = matchDay.sessions.map { session ->
                SessionInfo(
                    sessionId = session.id,
                    startTime = session.startTime
                )
            }

            val participantEntries = participants
                .filter { it.matchDayId == matchDay.id }
                .map { participant ->
                    ParticipantWithFunction(
                        userId = participant.userId,
                        category = participant.category,
                        function = participant.functionId.toString() //TODO: change to function name
                    )
                }

            MatchDayEntry(
                matchDayId = matchDay.id,
                matchDate = matchDay.matchDate,
                sessions = sessionEntries,
                participants = participantEntries
            )
        }

        val sealedCallList = callListMongoRepository.findByIntegerId(callList.id)

        return CallListDocument(
            _id = sealedCallList?._id,
            sqlId = callList.id,
            deadline = callList.deadline,
            callType = callList.callType,
            userId = callList.userId,
            competition = competitionInfo,
            matchDays = matchDayEntries
        )

    }

    fun getSealedCallList(callListId: String): Either<CallListError, CallListDocument> {
        val result = callListMongoRepository.findById(callListId)
        return if (result.isPresent) success(result.get())
        else failure(CallListError.CallListNotFound)
    }

}
