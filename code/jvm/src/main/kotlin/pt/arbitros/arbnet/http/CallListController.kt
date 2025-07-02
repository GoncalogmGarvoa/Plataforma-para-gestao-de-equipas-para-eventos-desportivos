@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.arbitros.arbnet.domain.Competition
import pt.arbitros.arbnet.http.model.calllist.CallListInputModel
import pt.arbitros.arbnet.http.model.ParticipantUpdateInput
import pt.arbitros.arbnet.http.model.calllist.CallListIdInput
import pt.arbitros.arbnet.http.model.calllist.EventOutputModel
import pt.arbitros.arbnet.services.*
import pt.arbitros.arbnet.services.callList.CallListService

@RestController
class CallListController(
    private val callListService: CallListService,
    private val usersService: UsersService,
    ) {

    @PostMapping(Uris.CallListUris.CREATE_CALLLIST)
    fun createCallList(
        @RequestBody callList: CallListInputModel,
        @RequestHeader token: String,
    ): ResponseEntity<*> {
        val userResult = usersService.getUserByToken(token)
        return if (userResult is Success) {
            when (
                val result = callListService.createEvent(callList,userResult.value.id)
            ) {
                is Success -> ResponseEntity.ok(result)
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
        } else {
            Problem.fromApiErrorToProblemResponse(
                ApiError.NotFound(
                    "User not found or not authorized to create a call list",
                ))
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
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
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
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }
    }


    @PutMapping(Uris.CallListUris.UPDATE_CALLLIST)
    fun updateCallList(
        @RequestBody callList: CallListInputModel,
        @RequestHeader token: String,
    ): ResponseEntity<*> {
        val userResult = usersService.getUserByToken(token)
        return if (userResult is Success) {
            when (
                val result = callListService.updateEvent(callList,userResult.value.id)
            ) {
                is Success -> ResponseEntity.ok(result)
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
        } else {
            Problem.fromApiErrorToProblemResponse(
                ApiError.NotFound(
                    "User not found or not authorized to create a call list",
                ))
        }
    }

    @GetMapping(Uris.CallListUris.GET_CALLLIST)
    fun getCallList(
        @PathVariable id: Int,
    ): ResponseEntity<*> =
        when (val result: Either<ApiError, EventOutputModel> = callListService.getEventById(id)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.CallListUris.GET_CALLLIST_DRAFT)
    fun getCallListDraft(
        @RequestHeader token: String,
    ): ResponseEntity<*> {

        val userResult = usersService.getUserByToken(token)
        return if (userResult is Success) {
            when (val result = callListService.getEventsDraft(userResult.value.id,"callList")) {
                is Success -> ResponseEntity.ok(result.value)
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
        } else {
            Problem.fromApiErrorToProblemResponse(
                ApiError.NotFound(
                    "User not found or not authorized to get call list draft",
                )
            )
        }
    }

    @GetMapping(Uris.CallListUris.GET_CALLLISTS_WITH_REFEREE)
    fun getAllCallListsWithReferee(
        @RequestBody refereeId: Int,
    ): ResponseEntity<*> =
        when (val result = callListService.getCallListsWithReferee(refereeId)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }


    @GetMapping(Uris.CallListUris.GET_SEALED_CALLLIST)
    fun getSealedCallList(
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = callListService.getSealedCallList(id)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PutMapping(Uris.CallListUris.CANCEL_CALLLIST)
    fun updateCallList(
        @RequestBody competitionId: Int,
    ): ResponseEntity<*> =
        when (
            val result = callListService.cancelCallList(competitionId)
        ) {
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }
}
