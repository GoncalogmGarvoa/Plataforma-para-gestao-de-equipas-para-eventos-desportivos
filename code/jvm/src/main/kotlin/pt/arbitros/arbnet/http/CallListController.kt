@file:Suppress("ktlint:standard:no-wildcard-imports")

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
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.CallListInputUpdateModel
import pt.arbitros.arbnet.http.model.ParticipantUpdateInput
import pt.arbitros.arbnet.services.ApiError
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
                    is ApiError.InvalidCompetitionName -> Problem.InvalidCompetitionName.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidAddress -> Problem.InvalidAddress.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidPhoneNumber -> Problem.InvalidPhoneNumber.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidAssociation -> Problem.InvalidAssociation.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidLocation -> Problem.InvalidLocation.response(HttpStatus.BAD_REQUEST)
                    is ApiError.MatchDayNotFound -> Problem.MatchDayNotFound.response(HttpStatus.NOT_FOUND)
                    is ApiError.ParticipantNotFound -> Problem.ParticipantNotFound.response(HttpStatus.NOT_FOUND)
                    is ApiError.ArbitrationCouncilNotFound ->
                        Problem.ArbitrationCouncilNotFound.response(
                            HttpStatus.NOT_FOUND,
                        )

                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to create the user")
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
                    is ApiError.CallListNotFound -> Problem.CallListNotFound.response(HttpStatus.NOT_FOUND)
                    is ApiError.ParticipantNotFound -> Problem.ParticipantNotFound.response(HttpStatus.NOT_FOUND)
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
                    is ApiError.CallListNotFound -> Problem.CallListNotFound.response(HttpStatus.NOT_FOUND)
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
                    value
                )
            }

            is Failure ->
                when (event.value) {
                    is ApiError.CallListNotFound -> Problem.CallListNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(event.value)
                }
        }

    @PutMapping(Uris.CallListUris.UPDATE_CALLLIST)
    fun updateCallList(
        @RequestBody callList: CallListInputUpdateModel,
    ): ResponseEntity<*> =
        when (
            val callList =
                callListService.updateEvent(
                    callList,
                )
        ) {
            is Success -> ResponseEntity.ok(callList)
            is Failure ->
                when (callList.value) {
                    is ApiError.CallListNotFound -> Problem.CallListNotFound.response(HttpStatus.NOT_FOUND)
                    is ApiError.InvalidCompetitionName -> Problem.InvalidCompetitionName.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidAddress -> Problem.InvalidAddress.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidPhoneNumber -> Problem.InvalidPhoneNumber.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidAssociation -> Problem.InvalidAssociation.response(HttpStatus.BAD_REQUEST)
                    is ApiError.InvalidLocation -> Problem.InvalidLocation.response(HttpStatus.BAD_REQUEST)
                    is ApiError.MatchDayNotFound -> Problem.MatchDayNotFound.response(HttpStatus.NOT_FOUND)
                    is ApiError.ParticipantNotFound -> Problem.ParticipantNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to create the user")
                }
        }

    @GetMapping(Uris.CallListUris.GET_SEALED_CALLLIST)
    fun getSealedCallList(
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val sealedCallList = callListService.getSealedCallList(id)) {
            is Success -> {
                val value = sealedCallList.value
                ResponseEntity.ok(
                    value
                )
            }

            is Failure ->
                when (sealedCallList.value) {
                    is ApiError.CallListNotFound -> Problem.CallListNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sealedCallList.value)
                }
        }
}
