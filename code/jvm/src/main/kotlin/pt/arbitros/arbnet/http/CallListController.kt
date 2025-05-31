@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.http

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
            val result =
                callListService.createEvent(callList)
        ) {
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
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

    @GetMapping(Uris.CallListUris.GET_CALLLIST)
    fun getCallList(
        @PathVariable id: Int,
    ): ResponseEntity<*> =
        when (val result = callListService.getEventById(id)) {
            is Success -> {
                val value = result.value
                ResponseEntity.ok(value)
            }
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PutMapping(Uris.CallListUris.UPDATE_CALLLIST)
    fun updateCallList(
        @RequestBody callList: CallListInputUpdateModel,
    ): ResponseEntity<*> =
        when (
            val result = callListService.updateEvent(callList)
        ) {
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.CallListUris.GET_SEALED_CALLLIST)
    fun getSealedCallList(
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = callListService.getSealedCallList(id)) {
            is Success -> {
                val value = result.value
                ResponseEntity.ok(value)
            }
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }
}
