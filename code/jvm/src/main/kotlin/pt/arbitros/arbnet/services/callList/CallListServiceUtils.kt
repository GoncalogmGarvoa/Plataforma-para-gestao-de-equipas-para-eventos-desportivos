package pt.arbitros.arbnet.services.callList

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.CallList
import pt.arbitros.arbnet.domain.CallListDomain
import pt.arbitros.arbnet.domain.Competition
import pt.arbitros.arbnet.domain.ConfirmationStatus
import pt.arbitros.arbnet.domain.MatchDay
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.domain.users.User
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.invalidFieldError
import pt.arbitros.arbnet.http.model.calllist.CallListInputModel
import pt.arbitros.arbnet.http.model.calllist.ParticipantChoice
import pt.arbitros.arbnet.repository.CallListRepository
import pt.arbitros.arbnet.repository.CompetitionRepository
import pt.arbitros.arbnet.repository.MatchDayRepository
import pt.arbitros.arbnet.repository.ParticipantRepository
import pt.arbitros.arbnet.repository.SessionsRepository
import pt.arbitros.arbnet.repository.Transaction
import pt.arbitros.arbnet.repository.UsersRepository
import pt.arbitros.arbnet.repository.adaptable_repos.EquipmentRepository
import pt.arbitros.arbnet.repository.adaptable_repos.FunctionRepository
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.Success
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.success
import java.time.LocalDate
import kotlin.collections.plusAssign

@Component
class CallListServiceUtils {

    fun validateUserInfo(
        callList: CallListInputModel,
        usersRepository: UsersRepository,
        callListDomain: CallListDomain,
        utilsDomain: UtilsDomain,
        userId: Int,
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
                callListDomain = callListDomain,
                utilsDomain = utilsDomain,
            )
        if (validateResult is Failure) return validateResult

        usersRepository.getUserById(userId)
            ?: return failure(
                ApiError.NotFound(
                    "User not found",
                    "No user found with the provided ID",
                )
            )

        if(!usersRepository.userHasCouncilRole(userId))
            return failure(
                ApiError.InvalidField(
                    "User does not have the required role",
                    "The user must have a council role to create or update a call list"
                )
            )

        callList.matchDaySessions.forEach { md ->
            if(md.matchDay.isBefore(LocalDate.now()) || md.matchDay.isBefore(callList.deadline)) {
                return failure(
                    ApiError.InvalidField(
                        "Invalid match day",
                        "Match day cannot be in the past or before the deadline: ${md.matchDay}",
                    )
                )
            }
        }


        val participants = callList.participants
        if (participants.isEmpty()) {
            return success(emptyList())
        }

        val participantIds = participants.map { it.userId }
        val foundReferees = usersRepository.getUsersAndCheckIfReferee(participantIds)

        if (foundReferees.size != participants.size) { //todo check
            return failure(
                ApiError.InvalidField(
                    "Invalid participants",
                    "Some participants are not referees or do not exist in the system",
                )
            )
        }
        return success(foundReferees)
    }

    fun createCompetitionAndSessions(
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
    ): Either<ApiError, Pair<Int, Map<LocalDate, Int>>> {// TODO review this the pair should be a data class

        val competitionId = competitionRepository.getCompetitionIdByCallListId(callList.callListId!!)

        competitionRepository.updateCompetition(
            competitionId,  // This function is only called when callList.callListId is not null
            callList.competitionName,
            callList.address,
            callList.phoneNumber,
            callList.email,
            callList.association,
            callList.location,
        )

        // 1. Delete existing match days and sessions

        sessionsRepository.deleteCompetitionSessions(competitionId)
        matchDayRepository.deleteCompetitionMatchDays(competitionId)

        // 2. Insert new match days and sessions
        val matchDayMap = mutableMapOf<LocalDate, Int>()
        callList.matchDaySessions.forEach { md ->
            val matchDayId = matchDayRepository.createMatchDay(competitionId, md.matchDay)
            matchDayMap[md.matchDay] = matchDayId

            md.sessions.forEach { tm ->
                sessionsRepository.createSession(competitionId, matchDayId, tm)
            }
        }


        return success(competitionId to matchDayMap)
    }

    fun createCallListOnly(
        callList: CallListInputModel,
        callListRepository: CallListRepository,
        competitionId: Int,
        userId: Int,
    ): Int =
        callListRepository.createCallList(
            callList.deadline,
            userId,
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

    fun createParticipantsOnly(
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
                            ?: return failure(
                                ApiError.NotFound(
                                    "Function not found",
                                    "No function found with the name '$funcName'",
                                )
                            )

                    val mdId =
                        matchDayMap[day]
                            ?: return failure(
                                ApiError.NotFound(
                                    "Match day not found",
                                    "No match day found for the date '$day'",
                                )
                            )

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

    fun validateCallList(
        competitionName: String,
        address: String,
        phoneNumber: String,
        email: String,
        association: String,
        location: String,
        callType: String,
        callListDomain : CallListDomain,
        utilsDomain: UtilsDomain,
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
                ?: return failure(
                    ApiError.NotFound(
                        "Competition not found",
                        "No competition found with the ID $competitionId"
                    )
                )

        return success(competition)
    }

    fun updateEventFull(
        callList: CallListInputModel,
        callListRepository: CallListRepository,
        competitionRepository: CompetitionRepository,
        matchDayRepository: MatchDayRepository,
        sessionsRepository: SessionsRepository,
        functionRepository: FunctionRepository,
        participantRepository: ParticipantRepository,
        equipmentRepository: EquipmentRepository,
    ): Either<ApiError, Int> {

        // 3. Update match days and sessions
        val result2 =
            updateCompetitionAndSessions(
                callList,
                competitionRepository,
                matchDayRepository,
                sessionsRepository,
            )
        if (result2 is Failure) return result2
        val (competitionId, matchDayMap) = (result2 as Success).value

        val callListId = updateCallListOnly(
            callList,
            callListRepository,
            competitionId
        )

        if (callList.participants.isNotEmpty()) {
            val participantsResult =
                createParticipantsOnly(
                    callList.participants,
                    matchDayMap,
                    callListId,
                    competitionId,
                    functionRepository,
                    participantRepository,
                )
            if (participantsResult is Failure) return participantsResult
        }

        // 4. Update equipments
        //todo uncomment when equipment is implemented
        if (callList.equipmentIds.isNotEmpty()) {
            if (!equipmentRepository.verifyEquipmentIds(callList.equipmentIds))
                return failure(
                    ApiError.InvalidField(
                        "Invalid equipment IDs",
                        "One or more equipment IDs provided do not exist in the database",
                    )
                )
            equipmentRepository.deleteEquipmentByCompetitionId(competitionId)
            equipmentRepository.selectEquipment(competitionId, callList.equipmentIds)
        }

        return success(callListId)
    }


    // Data class to hold the result of competition update only used in this class
    data class CompetitionUpdateResult(
        val competitionId: Int,
        val matchDayMap: Map<LocalDate, Int>
    )

}