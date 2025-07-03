package pt.arbitros.arbnet.services.callList

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.domain.CallListDocument
import pt.arbitros.arbnet.domain.CallListDomain
import pt.arbitros.arbnet.domain.CallListType
import pt.arbitros.arbnet.domain.CallListWithUserAndCompetition
import pt.arbitros.arbnet.domain.Competition
import pt.arbitros.arbnet.domain.CompetitionInfo
import pt.arbitros.arbnet.domain.MatchDay
import pt.arbitros.arbnet.domain.MatchDayEntry
import pt.arbitros.arbnet.domain.ParticipantWithFunction
import pt.arbitros.arbnet.domain.SessionInfo
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.ParticipantInfo
import pt.arbitros.arbnet.http.model.RefereeCallLists
import pt.arbitros.arbnet.http.model.RefereeCallListsOutputModel
import pt.arbitros.arbnet.http.model.calllist.CallListIdInput
import pt.arbitros.arbnet.http.model.calllist.CallListInputModel
import pt.arbitros.arbnet.http.model.calllist.EquipmentOutputModel
import pt.arbitros.arbnet.http.model.calllist.EventOutputModel
import pt.arbitros.arbnet.http.model.calllist.ParticipantWithCategory
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.repository.mongo.CallListMongoRepository
import pt.arbitros.arbnet.services.callList.CallListServiceUtils
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.Success
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.success
import pt.arbitros.arbnet.transactionRepo

@Component
class CallListService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val callListMongoRepository: CallListMongoRepository,
    private val utilsDomain: UtilsDomain,
    private val callListDomain: CallListDomain,
    private val callListUtils: CallListServiceUtils,
    private val callListServiceUtils: CallListServiceUtils
    // private val clock: Clock
) {

    // todo Event > callList + competition
    fun createEvent(callList: CallListInputModel, userId: Int): Either<ApiError, Int> =
        transactionManager.run {
            val result = callListUtils.validateUserInfo(
                callList,
                it.usersRepository,
                callListDomain,
                utilsDomain,
                userId
            )
            if (result is Failure) return@run result

            try {
                val (competitionId, matchDayMap) =
                callListUtils.createCompetitionAndSessions(
                    callList,
                    it.competitionRepository,
                    it.matchDayRepository,
                    it.sessionsRepository,
                )

                val callListId =
                    callListUtils.createCallListOnly(
                        callList,
                        it.callListRepository,
                        competitionId,
                        userId,
                    )

                if (callList.participants.isNotEmpty()) {
                    val participantsResult =
                        callListUtils.createParticipantsOnly(
                            callList.participants,
                            matchDayMap,
                            callListId,
                            competitionId,
                            it.functionRepository,
                            it.participantRepository,
                            it.notificationRepository
                        )
                    if (participantsResult is Failure)
                        throw RuntimeException("Error Creating Participant: ${participantsResult.value}")

                }

                if (callList.equipmentIds.isNotEmpty()) {
                    it.equipmentRepository.verifyEquipmentIds(callList.equipmentIds)
                    it.equipmentRepository.selectEquipment(competitionId, callList.equipmentIds)
                }

                success(callListId)
            }
            catch (e: Exception) {
                it.rollback()
                return@run failure(
                    ApiError.InvalidField(
                        e.message ?: "Error creating event",
                        e.message ?: "Unknown error"
                    )
                )
            }

        }

    fun updateEvent(callList: CallListInputModel, userId: Int): Either<ApiError, Int> =
        transactionManager.run {
            // check if callList with id exists
            if (callList.callListId == null) {
                return@run failure(
                    ApiError.InvalidField(
                        "CallList ID is required",
                        "CallList ID must be provided for updating an existing call list",
                    )
                )
            }

            it.callListRepository.getCallListById(callList.callListId)
                ?: return@run failure(
                    ApiError.NotFound(
                        "CallList with id ${callList.callListId} not found"
                    )
                )

            val result = callListUtils.validateUserInfo(
                callList,
                it.usersRepository,
                callListDomain,
                utilsDomain,
                userId
            )
            if (result is Failure) return@run result

            val updateEventResult = callListUtils.updateEventFull(
                callList,
                it.callListRepository,
                it.competitionRepository,
                it.matchDayRepository,
                it.sessionsRepository,
                it.functionRepository,
                it.participantRepository,
                it.notificationRepository,
                it.equipmentRepository,
            )

            if (updateEventResult is Failure) return@run updateEventResult

            success((updateEventResult as Success<Int>).value)
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
                ?: return@run failure(
                    ApiError.NotFound(
                        "Participant not found",
                        "No participant found with the ID $participantId"
                    )
                )

            // Check if the call list exists
            callListRepository.getCallListById(callListId)
                ?: return@run failure(
                    ApiError.NotFound(
                        "Call list not found",
                        "No call list found with the ID $callListId"
                    )
                )

            participantRepository.updateParticipantConfirmationStatus(days, participantId, callListId)
            if (participantRepository.isCallListDone(callListId)) {
                callListRepository.updateCallListStatus(callListId)
            }
            return@run success(true)
        }

    fun getEventById(id: Int): Either<ApiError, EventOutputModel> =
        transactionManager.run { tx ->
            val callListRepository = tx.callListRepository

            val callList =
                callListRepository.getCallListById(id)
                    ?: return@run failure(
                        ApiError.InvalidField(
                            "CallList not found",
                            "No call list found with the ID $id",
                        )
                    )

            val competitionResult = callListUtils.getCompetitionById(tx, callList.competitionId)
            val participantsResult = callListUtils.getParticipantsByCallList(tx, callList)
            val matchDaysResult = callListUtils.getMatchDaysByCompetitionId(tx, callList.competitionId)

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
                    ?: return@run failure(
                        ApiError.InvalidField(
                            "Participant does not have a category",
                            "User with ID ${it.userId} does not have a category assigned",
                        )
                    )
                val category = tx.categoryRepository.getCategoryNameById(categoryId)
                    ?: return@run failure(
                        ApiError.InvalidField(
                            "Category not found",
                            "No category found with ID $categoryId",
                        )
                    )
                val user = tx.usersRepository.getUserById(it.userId)
                    ?: return@run failure(
                        ApiError.NotFound(
                            "User not found",
                            "No user found with ID ${it.userId}",
                        )
                    )

                val function = tx.functionRepository.getFunctionNameById(it.functionId)
                    ?: return@run failure(
                        ApiError.NotFound(
                            "Function not found",
                            "No function found with ID ${it.functionId}",
                        )
                    )


                ParticipantWithCategory(
                    callListId = it.callListId,
                    matchDayId = it.matchDayId,
                    competitionIdMatchDay = it.competitionIdMatchDay,
                    userId = it.userId,
                    userName = user.name,
                    functionId = it.functionId,
                    functionName = function,
                    confirmationStatus = it.confirmationStatus,
                    category = category,
                )
            }

            val equipments = tx.equipmentRepository.getEquipmentByCompetitionId(callList.competitionId)
                .map { equipment -> EquipmentOutputModel(id = equipment.id, name = equipment.name,) }

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
                    equipments = equipments,
                )
            return@run success(event)
        }



    fun getEventsDraft(userId: Int,callListType: String): Either<ApiError, List<CallListWithUserAndCompetition>> =
        transactionManager.run { tx ->
            val callLists = tx.callListRepository.getCallListsByUserIdAndType(userId, callListType)
            success(callLists)
        }

    fun getCallListsWithReferee(refereeId: Int): Either<ApiError, List<RefereeCallListsOutputModel>> {
        return transactionManager.run { tx ->
            val callLists: List<RefereeCallLists> = tx.callListRepository.getCallListsWithReferee(refereeId)
            if (callLists.isEmpty()) {
                return@run failure(
                    ApiError.NotFound(
                        "No call lists found for referee with ID $refereeId",
                        "The referee with ID $refereeId has no associated call lists."
                    )
                )
            }


            val final = callLists.map {

                val participants = tx.participantRepository.getParticipantsByCallList(it.callListId)

                val participantsInfo = participants.map { participant ->
                    val categoryId = tx.categoryDirRepository.getCategoryIdByUserId(participant.userId)
                        ?: return@run failure(
                            ApiError.InvalidField(
                                "Participant does not have a category",
                                "User with ID ${participant.userId} does not have a category assigned",
                            )
                        )
                    val category = tx.categoryRepository.getCategoryNameById(categoryId)
                        ?: return@run failure(
                            ApiError.InvalidField(
                                "Category not found",
                                "No category found with ID $categoryId",
                            )
                        )
                    val user = tx.usersRepository.getUserById(participant.userId)
                        ?: return@run failure(
                            ApiError.NotFound(
                                "User not found",
                                "No user found with ID ${participant.userId}",
                            )
                        )

                    val function = tx.functionRepository.getFunctionNameById(participant.functionId)
                        ?: return@run failure(
                            ApiError.NotFound(
                                "Function not found",
                                "No function found with ID ${participant.functionId}",
                            )
                        )

                    ParticipantInfo(
                        user.name,
                        category,
                        function,
                        participant.confirmationStatus,
                        participant.userId,
                        participant.matchDayId
                    )
                }

                val matchDays = tx.matchDayRepository.getMatchDaysByCompetition(it.competitionId)
                val equipments = tx.equipmentRepository.getEquipmentByCompetitionId(it.competitionId)
                    .map { equipment -> EquipmentOutputModel(id = equipment.id, name = equipment.name,) }


                RefereeCallListsOutputModel(
                    it.callListId,
                    it.competitionName,
                    it.address,
                    it.phoneNumber,
                    it.email,
                    it.association,
                    it.location,
                    it.deadline.toString(),
                    it.callListType,
                    participantsInfo,
                    matchDays,
                    equipments
                )
            }
            return@run success(final)
        }
    }

    fun updateCallListStage(callListId: Int): Either<ApiError, Boolean> =
        transactionManager.run { tx ->
            val callListRepository = tx.callListRepository

            val callList =
                callListRepository.getCallListById(callListId)
                    ?: return@run failure(
                        ApiError.NotFound(
                            "CallList not found",
                            "No call list found with the ID $callListId",
                        )
                    )

            val participants = tx.participantRepository.getParticipantsByCallList(callListId)

            if (participants.isEmpty()) {
                return@run failure(
                    ApiError.InvalidField(
                        "No participants found",
                        "In order to seal the call list, there must be at least one participant.",
                    )
                )
            }


            //TODO code below is repeated from above put all in one function
            val callListContent = callListRepository.getCallListById(callListId)!!

            val competitionInfo = tx.competitionRepository.getCompetitionById(callListContent.competitionId)
                ?: return@run failure(
                    ApiError.NotFound(
                        "Competition not found",
                        "No competition found with the ID ${callListContent.competitionId}",
                    )
                )

            val participantsWithCategory = participants.map{
                val categoryId = tx.categoryDirRepository.getCategoryIdByUserId(it.userId)
                    ?: return@run failure(
                        ApiError.InvalidField(
                            "Participant does not have a category",
                            "User with ID ${it.userId} does not have a category assigned",
                        )
                    )
                val category = tx.categoryRepository.getCategoryNameById(categoryId)
                    ?: return@run failure(
                        ApiError.InvalidField(
                            "Category not found",
                            "No category found with ID $categoryId",
                        )
                    )

                val user = tx.usersRepository.getUserById(it.userId)
                    ?: return@run failure(
                        ApiError.NotFound(
                            "User not found",
                            "No user found with ID ${it.userId}",
                        )
                    )

                val function = tx.functionRepository.getFunctionNameById(it.functionId)?:
                    return@run failure(
                        ApiError.NotFound(
                            "Function not found",
                            "No function found with ID ${it.functionId}",
                        )
                    )

            val callType =
                when (callList.callType) {
                    CallListType.CALL_LIST.callType -> {
                        CallListType.SEALED_CALL_LIST.callType
                    }
                    CallListType.CONFIRMATION.callType -> {
                        CallListType.FINAL_JURY.callType
                    }
                    else -> return@run failure(
                        ApiError.InvalidField(
                            "Invalid call list type",
                            "Call list must either be in 'CALL_LIST' or 'CONFIRMATION' to update the stage.",
                        )
                    )
                }

            callListRepository.updateCallListStage(callListId, callType)

                ParticipantWithCategory(
                    callListId = it.callListId,
                    matchDayId = it.matchDayId,
                    competitionIdMatchDay = it.competitionIdMatchDay,
                    userId = it.userId,
                    userName = user.name,
                    functionId = it.functionId,
                    functionName = function,
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
                        userName = participant.userName,
                        category = participant.category,
                        function = participant.functionName
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
        else failure(
            ApiError.NotFound(
                "CallList not found",
                "No call list found with the ID $callListId",
            )
        )
    }

    fun cancelCallList(competitionId: Int) : Either<ApiError, Boolean> {
        transactionManager.run { tx ->
            val callListRepository = tx.callListRepository
            val competitionRepository = tx.competitionRepository

            // Check if the competition exists
            competitionRepository.getCompetitionById(competitionId)
                ?: return@run failure(
                    ApiError.NotFound(
                        "Competition not found",
                        "No competition found with ID $competitionId",
                    )
                )

            val callList = callListRepository.getCallListsByCompetitionId(competitionId)
                ?: return@run failure(
                    ApiError.NotFound(
                        "CallList not found",
                        "No call list found for competition with ID $competitionId",
                    )
                )

            when (callList.callType) {
                CallListType.CALL_LIST.callType -> {
                    competitionRepository.deleteCompetition(competitionId)
                }
                else -> {
                    callListRepository.updateCallListStage(
                        callList.id,
                        CallListType.CANCELLED.callType
                    )
                }
            }

        }
        return success(true)
    }



}