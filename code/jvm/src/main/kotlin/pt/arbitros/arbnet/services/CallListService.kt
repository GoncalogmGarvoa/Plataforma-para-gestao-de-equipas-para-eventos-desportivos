@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.*
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.EquipmentOutputModel
import pt.arbitros.arbnet.http.model.EventOutputModel
import pt.arbitros.arbnet.http.model.ParticipantWithCategory
import pt.arbitros.arbnet.repository.*
import pt.arbitros.arbnet.repository.mongo.CallListMongoRepository
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
                    )
                if (participantsResult is Failure) return@run participantsResult
            }

            if (callList.equipmentIds.isNotEmpty()) {
                it.equipmentRepository.verifyEquipmentId(callList.equipmentIds)
                it.equipmentRepository.selectEquipment(competitionId, callList.equipmentIds)
            }

            success(callListId)
        }

    fun updateEvent(callList: CallListInputModel,userId: Int): Either<ApiError, Int> =
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

    fun getEventById(id: Int): Either<ApiError, EventOutputModel> =
        transactionManager.run { tx ->
            val callListRepository = tx.callListRepository

            val callList =
                callListRepository.getCallListById(id)
                    ?: return@run failure(ApiError.InvalidField(
                        "CallList not found",
                        "No call list found with the ID $id",
                    ))

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
                    ?: return@run failure(ApiError.InvalidField(
                        "Participant does not have a category",
                        "User with ID ${it.userId} does not have a category assigned",
                    ))
                val category = tx.categoryRepository.getCategoryNameById(categoryId)
                    ?: return@run failure(ApiError.InvalidField(
                        "Category not found",
                        "No category found with ID $categoryId",
                    ))
                val user = tx.usersRepository.getUserById(it.userId)
                    ?: return@run failure(ApiError.NotFound(
                        "User not found",
                        "No user found with ID ${it.userId}",
                    ))

                ParticipantWithCategory(
                    callListId = it.callListId,
                    matchDayId = it.matchDayId,
                    competitionIdMatchDay = it.competitionIdMatchDay,
                    userId = it.userId,
                    userName = user.name,
                    functionId = it.functionId,
                    confirmationStatus = it.confirmationStatus,
                    category = category,
                )
            }

            val equipments = tx.equipmentRepository.getEquipmentByCompetitionId(callList.competitionId)
                .map { equipment -> EquipmentOutputModel(id = equipment.id, name = equipment.name,)}

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

                val user = tx.usersRepository.getUserById(it.userId)
                    ?: return@run failure(ApiError.NotFound(
                        "User not found",
                        "No user found with ID ${it.userId}",
                    ))

                ParticipantWithCategory(
                    callListId = it.callListId,
                    matchDayId = it.matchDayId,
                    competitionIdMatchDay = it.competitionIdMatchDay,
                    userId = it.userId,
                    userName = user.name,
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
