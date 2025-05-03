package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.ParticipantUpdateInput
import pt.arbitros.arbnet.http.model.Problem
import pt.arbitros.arbnet.http.model.RoleAssignmentsInput
import pt.arbitros.arbnet.services.CallListError
import pt.arbitros.arbnet.services.CallListService
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.Success

@RestController
class CallListController(
    private val callListService: CallListService,
) {
    @PostMapping(Uris.CallListUris.CREATE_CALLLIST)
    fun createUser(
        @RequestBody callList: CallListInputModel,
    ): ResponseEntity<*> =
        when (
            val callList =
                callListService.createCallList(
                    callList.competitionName,
                    callList.address,
                    callList.phoneNumber,
                    callList.email,
                    callList.association,
                    callList.location,
                    callList.deadline,
                    callList.councilId,
                    callList.participant,
                    callList.matchDaySessions,
                )
        ) {
            is Success -> ResponseEntity.ok(callList)
            is Failure ->
                when (callList.value) {
                    is CallListError.MatchDayNotFound -> Problem.MatchDayNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.ParticipantNotFound -> Problem.ParticpantNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.ArbitrationCouncilNotFound -> Problem.ArbitrationCouncilNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to create the user")
                }
        }

    @PutMapping(Uris.CallListUris.ASSIGN_ROLES)
    fun assingRoles(
        @RequestBody roleAssignmentsInfo: List<RoleAssignmentsInput>,
    ): ResponseEntity<*> {
        val result =
            callListService.assignRoles(
                roleAssignmentsInfo,
            )
        return when (result) {
            is Success -> ResponseEntity.ok("Role successfully assigned")
            is Failure -> {
                val error = result.value
                when (error) {
                    is CallListError.RoleNotFound -> Problem.RoleNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.MatchDayNotFound -> Problem.MatchDayNotFound.response(HttpStatus.NOT_FOUND)
                    is CallListError.ParticipantNotFound -> Problem.ParticpantNotFound.response(HttpStatus.NOT_FOUND)
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
                    is CallListError.ParticipantNotFound -> Problem.ParticpantNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to change the confirmation status")
                }
            }
        }
    }
}
