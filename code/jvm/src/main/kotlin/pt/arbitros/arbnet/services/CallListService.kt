@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.*
import pt.arbitros.arbnet.domain.users.User
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.invalidFieldError
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.EventOutputModel
import pt.arbitros.arbnet.http.model.ParticipantChoice
import pt.arbitros.arbnet.http.model.ParticipantWithCategory
import pt.arbitros.arbnet.repository.*
import pt.arbitros.arbnet.repository.mongo.CallListMongoRepository
import pt.arbitros.arbnet.transactionRepo
import java.time.LocalDate

@Component
class CallListService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val callListMongoRepository: CallListMongoRepository,
    private val utilsDomain: UtilsDomain,
    private val callListDomain: CallListDomain,
    // private val clock: Clock
) {

    // todo Event > callList + competition
    fun createEvent(callList: CallListInputModel): Either<ApiError, Int> =
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
                )

            if (callList.participants.isNotEmpty()) {
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

    fun updateEvent(callList: CallListInputModel): Either<ApiError, Int> =
        transactionManager.run {
            // check if callList with id exists
            if (callList.callListId == null) {
                return@run failure(ApiError.InvalidField(
                    "CallList ID is required",
                    "CallList ID must be provided for updating an existing call list",
                ))
            }

            it.callListRepository.getCallListById(callList.callListId)
                ?: return@run failure(ApiError.NotFound(
                    "CallList with id ${callList.callListId} not found"
                ))

            val result = validateAndCheckUsers(callList, it.usersRepository)
            if (result is Failure) return@run result

            val (competitionId, matchDayMap) =
                updateCompetitionAndSessions(
                    callList,
                    it.competitionRepository,
                    it.matchDayRepository,
                    it.sessionsRepository,
                )

            val callListId = updateCallListOnly(
                    callList,
                    it.callListRepository,
                    competitionId,
                )

            if (callList.participants.isNotEmpty()) {
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
        callList: CallListInputModel,
        usersRepository: UsersRepository,
    ): Either<ApiError, List<User>>? {
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
            ?: return failure(ApiError.NotFound(
                "User not found",
                "No user found with the provided ID",
            ))

         if(!usersRepository.userHasCouncilRole(callList.userId))
             return failure(ApiError.InvalidField(
                "User does not have the required role",
                "The user must have a council role to create or update a call list"
             ))


        val participants = callList.participants
        if (participants.isEmpty()) {
            return success(emptyList())
        }

        val participantIds = participants.map { it.userId }
        val foundReferees = usersRepository.getUsersAndCheckIfReferee(participantIds)

        if (foundReferees.size != participants.size) {
            return failure(ApiError.InvalidField(
                "Invalid participants",
                "Some participants are not referees or do not exist in the system",
            ))
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
        callList: CallListInputModel,
        competitionRepository: CompetitionRepository,
        matchDayRepository: MatchDayRepository,
        sessionsRepository: SessionsRepository,
    ): Pair<Int, Map<LocalDate, Int>> {
        val competitionId =
            competitionRepository.updateCompetition(
                callList.callListId!!,  // This function is only called when callList.callListId is not null
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
        callList: CallListInputModel,
        callListRepository: CallListRepository,
        competitionId: Int,
    ): Int =
        callListRepository.updateCallList(
            callList.callListId!!,
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
    ): Either<ApiError, Unit> {
        val participantsToInsert = mutableListOf<Participant>()

        if (participants != null) {
            for (p in participants) {
                for ((day, funcName) in p.participantAndRole) {
                    if (funcName.isBlank()) continue

                    val funcId =
                        functionRepository.getFunctionIdByName(funcName)
                            ?: return failure(ApiError.NotFound(
                                "Function not found",
                                "No function found with the name '$funcName'",
                            ))

                    val mdId =
                        matchDayMap[day]
                            ?: return failure(ApiError.NotFound(
                                "Match day not found",
                                "No match day found for the date '$day'",
                            ))

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
    ): Either<ApiError, Boolean> =
        transactionManager.run {
            val participantRepository = it.participantRepository
            val callListRepository = it.callListRepository

            // Check if the participant exists
            participantRepository.getParticipantById(participantId)
                ?: return@run failure(ApiError.NotFound(
                    "Participant not found",
                    "No participant found with the ID $participantId"
                ))

            // Check if the call list exists
            callListRepository.getCallListById(callListId)
                ?: return@run failure(ApiError.NotFound(
                    "Call list not found",
                    "No call list found with the ID $callListId"
                ))

            participantRepository.updateParticipantConfirmationStatus(days, participantId, callListId)
            if (participantRepository.isCallListDone(callListId)) {
                callListRepository.updateCallListStatus(callListId)
            }
            return@run success(true)
        }

    fun getParticipantsByCallList(
        tx: Transaction,
        callList: CallList,
    ): Either<ApiError, List<Participant>> {
        val participantRepository = tx.participantRepository

        val participants = participantRepository.getParticipantsByCallList(callList.id)
        return success(participants)
    }

    fun getCompetitionById(
        tx: Transaction,
        competitionId: Int,
    ): Either<ApiError, Competition> {
        val competitionRepository = tx.competitionRepository

        val competition =
            competitionRepository.getCompetitionById(competitionId)
                ?: return failure(ApiError.NotFound(
                    "Competition not found",
                    "No competition found with the ID $competitionId"
                ))

        return success(competition)
    }

    //TODO this does not need to return an Either, it can return a List<MatchDay>, the list can be empty
    fun getMatchDaysByCompetitionId(
        tx: Transaction,
        competitionId: Int,
    ): Either<ApiError, List<MatchDay>> {
        val matchDayRepository = tx.matchDayRepository

        val matchDays =
            matchDayRepository.getMatchDaysByCompetition(competitionId)

        return success(matchDays)
    }

    fun getEventById(id: Int): Either<ApiError, EventOutputModel> =
        transactionManager.run { tx ->
            val callListRepository = tx.callListRepository

            val callList =
                callListRepository.getCallListById(id)
                    ?: return@run failure(ApiError.InvalidField(
                        "CallList not found",
                        "No call list found with the ID $id",
                    ))

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
                    ?: return@run failure(ApiError.InvalidField(
                        "Participant does not have a category",
                        "User with ID ${it.userId} does not have a category assigned",
                    ))
                val category = tx.categoryRepository.getCategoryNameById(categoryId)
                    ?: return@run failure(ApiError.InvalidField(
                        "Category not found",
                        "No category found with ID $categoryId",
                    ))

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
    ): Either<ApiError, Unit> {
        if (!callListDomain.validCallListType(callType)) return failure(invalidFieldError("callType"))
        if (!utilsDomain.validName(competitionName)) return failure(invalidFieldError("competitionName"))
        if (!utilsDomain.validAddress(address)) return failure(invalidFieldError("address"))
        if (!utilsDomain.validPhoneNumber(phoneNumber)) return failure(invalidFieldError("phoneNumber"))
        if (!utilsDomain.validEmail(email)) return failure(invalidFieldError("email"))
        if (!utilsDomain.validName(association)) return failure(invalidFieldError("association"))
        if (!utilsDomain.validName(location)) return failure(invalidFieldError("location"))

        return success(Unit)
    }

fun updateCallListStage(callListId: Int): Either<ApiError, Boolean> =
    transactionManager.run { tx ->
        val callListRepository = tx.callListRepository

        val callList =
            callListRepository.getCallListById(callListId)
                ?: return@run failure(ApiError.NotFound(
                    "CallList not found",
                    "No call list found with the ID $callListId",
                ))

        val participants = tx.participantRepository.getParticipantsByCallList(callListId)

        if (participants.isEmpty()) {
            return@run failure(ApiError.InvalidField(
                "No participants found",
                "In order to seal the call list, there must be at least one participant.",
            ))
        }

        val callType =
            when (callList.callType) {
                CallListType.CALL_LIST.callType -> {
                    CallListType.SEALED_CALL_LIST.callType
                }
                CallListType.CONFIRMATION.callType -> {
                    CallListType.FINAL_JURY.callType
                }
                else -> return@run failure(ApiError.InvalidField(
                    "Invalid call list type",
                    "Call list must either be in 'CALL_LIST' or 'CONFIRMATION' to update the stage.",
                ))
            }

        callListRepository.updateCallListStage(callListId, callType)

        //TODO code below is repeated from above put all in one function
        val callListContent = callListRepository.getCallListById(callListId)!!

        val competitionInfo = tx.competitionRepository.getCompetitionById(callListContent.competitionId)
            ?: return@run failure(ApiError.NotFound(
                "Competition not found",
                "No competition found with the ID ${callListContent.competitionId}",
            ))

        val participantsWithCategory = participants.map{
            val categoryId = tx.categoryDirRepository.getCategoryIdByUserId(it.userId)
                ?: return@run failure(ApiError.InvalidField(
                    "Participant does not have a category",
                    "User with ID ${it.userId} does not have a category assigned",
                ))
            val category = tx.categoryRepository.getCategoryNameById(categoryId)
                ?: return@run failure(ApiError.InvalidField(
                    "Category not found",
                    "No category found with ID $categoryId",
                ))

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

    fun getSealedCallList(callListId: String): Either<ApiError, CallListDocument> {
        val result = callListMongoRepository.findById(callListId)
        return if (result.isPresent) success(result.get())
        else failure(ApiError.NotFound(
            "CallList not found",
            "No call list found with the ID $callListId",
            )
        )
    }

}
