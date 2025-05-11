package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.http.model.*
import pt.arbitros.arbnet.services.CallListError
import pt.arbitros.arbnet.services.CallListService
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.Success

@RestController
class CallListController(
    private val callListService: CallListService,
) {
    @PostMapping(Uris.CallListUris.CREATE_CALLLIST)
    fun createCallList(
        @RequestBody callList: CallListInputModel,
    ): ResponseEntity<*> =
        when (
            val callList =
                callListService.createEvent(
                    callList,
                )
        ) {
            is Success -> ResponseEntity.ok(callList)
            is Failure ->
                when (callList.value) {
                    is CallListError.InvalidCompetitionName -> Problem.InvalidCompetitionName.response(HttpStatus.BAD_REQUEST)
                    is CallListError.InvalidAddress -> Problem.InvalidAddress.response(HttpStatus.BAD_REQUEST)
                    is CallListError.InvalidPhoneNumber -> Problem.InvalidPhoneNumber.response(HttpStatus.BAD_REQUEST)
                    is CallListError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
                    is CallListError.InvalidAssociation -> Problem.InvalidAssociation.response(HttpStatus.BAD_REQUEST)
                    is CallListError.InvalidLocation -> Problem.InvalidLocation.response(HttpStatus.BAD_REQUEST)
                    is CallListError.MatchDayNotFound -> Problem.MatchDayNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.ParticipantNotFound -> Problem.ParticipantNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.ArbitrationCouncilNotFound ->
                        Problem.ArbitrationCouncilNotFound.response(
                            HttpStatus.NOT_FOUND,
                        )

                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to create the user")
                }
        }

    // Not in use for now
    @PutMapping(Uris.CallListUris.ASSIGN_ROLES)
    fun assignRoles(
        @RequestBody roleAssignmentsInfo: List<FunctionsAssignmentsInput>,
    ): ResponseEntity<*> {
        val result =
            callListService.assignFunction(
                roleAssignmentsInfo,
            )
        return when (result) {
            is Success -> ResponseEntity.ok("Role successfully assigned")
            is Failure -> {
                val error = result.value
                when (error) {
                    is CallListError.FunctionNotFound -> Problem.RoleNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.MatchDayNotFound -> Problem.MatchDayNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.ParticipantNotFound -> Problem.ParticipantNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to assign the role")
                }
            }
        }
    }

    @PutMapping(Uris.CallListUris.UPDATE_PARTICIPANT_CONFIRMATION_STATUS)
    fun updateParticipantConfirmationStatus(
        @RequestBody participantUpdate: ParticipantUpdateInput,
    ): ResponseEntity<*> {
        val result =
            callListService.updateParticipantConfirmationStatus(
                participantUpdate.days,
                participantUpdate.participantId,
                participantUpdate.callListId,
            )
        return when (result) {
            is Success -> ResponseEntity.ok("confirmation status changed")
            is Failure -> {
                val error = result.value
                when (error) {
                    is CallListError.CallListNotFound -> Problem.CallListNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.ParticipantNotFound -> Problem.ParticipantNotFound.response(HttpStatus.NOT_FOUND)
                    else ->
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Failed to change the confirmation status")
                }
            }
        }
    }

    @PutMapping(Uris.CallListUris.UPDATE_CALLLISTSTAGE)
    fun updateCallListStage(
        @RequestBody callListId: CallListIdInput,
    ): ResponseEntity<*> {
        val result =
            callListService.updateCallListStage(callListId.id)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> {
                val error = result.value
                when (error) {
                    is CallListError.CallListNotFound -> Problem.CallListNotFound.response(HttpStatus.NOT_FOUND)
                    else ->
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Failed to change the Stage of callList")
                }
            }
        }
    }

    @GetMapping(Uris.CallListUris.GET_CALLLIST)
    fun getCallList(
        @PathVariable id: Int,
    ): ResponseEntity<*> =
        when (val event = callListService.getEventById(id)) {
            is Success -> {
                val value = event.value
                ResponseEntity.ok(
                    EventOutputModel(
                        value.competitionName,
                        value.address,
                        value.phoneNumber,
                        value.email,
                        value.association,
                        value.location,
                        value.userId,
                        value.participants,
                        value.deadline,
                        value.callListType,
                        value.matchDaySessions,
                    ),
                )
            }

            is Failure ->
                when (event.value) {
                    is CallListError.CallListNotFound -> Problem.CallListNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(event.value)
                }
        }
}
