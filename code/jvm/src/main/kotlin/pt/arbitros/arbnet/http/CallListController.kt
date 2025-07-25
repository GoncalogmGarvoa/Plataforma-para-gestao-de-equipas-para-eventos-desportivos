@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.arbitros.arbnet.domain.CallListType
import pt.arbitros.arbnet.http.model.ParticipantUpdateInput
import pt.arbitros.arbnet.http.model.calllist.*
import pt.arbitros.arbnet.http.model.users.ParticipantUpdateInputArbitrationCouncil
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
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<*> {
        val token = authorizationHeader.removePrefix("Bearer ").removePrefix("bearer ")
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
        @RequestHeader("Authorization") authorizationHeader: String,

        ): ResponseEntity<*> {
        val token = authorizationHeader.removePrefix("Bearer ").removePrefix("bearer ")
        val userResult = usersService.getUserByToken(token)

        return if (userResult is Success) {
            val result =
                callListService.updateParticipantConfirmationStatus(
                        participantUpdate.days,
                    userResult.value.id,
                    participantUpdate.callListId,
                )
            return when (result) {
                is Success -> ResponseEntity.ok("confirmation status changed")
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
        }
        else {
            Problem.fromApiErrorToProblemResponse(
                ApiError.NotFound(
                    "User not found or not authorized to update participant confirmation status",
                ))
        }
    }

    @PutMapping(Uris.CallListUris.UPDATE_PARTICIPANT_CONFIRMATION_STATUS_ARBITRATION_COUNCIL)
    fun updateParticipantConfirmationStatusFromArbitrationCouncil(
        @RequestBody participantUpdate: ParticipantUpdateInputArbitrationCouncil,
        ): ResponseEntity<*> {
        val result =
            callListService.updateParticipantConfirmationStatus(
                participantUpdate.days,
                participantUpdate.userId,
                participantUpdate.callListId,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
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
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<*> {
        val token = authorizationHeader.removePrefix("Bearer ").removePrefix("bearer ")
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

//    @GetMapping(Uris.CallListUris.GET_CALLLIST_BY_TYPE)
//    fun getCallListDraft(
//        @RequestHeader("Authorization") authorizationHeader: String,
//        @RequestParam callType: String,
//    ): ResponseEntity<*> {
//        val token = authorizationHeader.removePrefix("Bearer ").removePrefix("bearer ")
//        val userResult = usersService.getUserByToken(token)
//        return if (userResult is Success) {
//            when (val result = callListService.getEventsDraft(userResult.value.id, callType)) {
//                is Success -> ResponseEntity.ok(result.value)
//                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
//            }
//        } else {
//            Problem.fromApiErrorToProblemResponse(
//                ApiError.NotFound(
//                    "User not found or not authorized to get call list draft",
//                )
//            )
//        }
//    }

    @GetMapping(Uris.CallListUris.GET_CALLLIST_BY_TYPE)
    fun getCallListDraft(
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestParam callType: String,
        @RequestParam(required = false, defaultValue = "0") offset: Int,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
    ): ResponseEntity<*> {
        val token = authorizationHeader.removePrefix("Bearer ").removePrefix("bearer ")
        val userResult = usersService.getUserByToken(token)
        return if (userResult is Success) {
            when (val result = callListService.getEventsDraft(userResult.value.id, callType, offset,limit)) {
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
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<*> {
        val token = authorizationHeader.removePrefix("Bearer ").removePrefix("bearer ")
        val userResult = usersService.getUserByToken(token)
        return if (userResult is Success) {
            when (
                val result = callListService.getCallListsWithReferee(userResult.value.id, page, size)
            ) {
                is Success -> ResponseEntity.ok(result.value)
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
        } else {
            Problem.fromApiErrorToProblemResponse(
                ApiError.NotFound("User not found or not authorized to create a call list")
            )
        }
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
    fun cancelCallList(
        @RequestBody request: CancelCallListInputModel,
    ): ResponseEntity<*> =
        when (
            val result = callListService.cancelCallList(request.callListId)
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }


    @GetMapping(Uris.CallListUris.GET_CALLLISTS_FINAL_JURY_FUNCTION)
    fun getCallListsFinalJuryJa(
        @PathVariable function: String,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<*> {
        val token = authorizationHeader.removePrefix("Bearer ").removePrefix("bearer ")
        val userResult = usersService.getUserByToken(token)
        val callListType = CallListType.FINAL_JURY.callType
        val reportType = if( function.uppercase() == "JA") {
            "JA_REPORT"
        } else {
            "DEL_REPORT"
        }
        return if (userResult is Success) {
            when (
                val result: Either<ApiError, List<CallListReportOutputModel>> = callListService.getCallListsFinalJuryFunction(userResult.value.id,callListType, function.uppercase(), reportType)
            ) {
                is Success -> ResponseEntity.ok(result.value)
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
        } else {
            Problem.fromApiErrorToProblemResponse(
                ApiError.NotFound(
                    "User not found or not authorized to get call lists final jury ja",
                )
            )
        }
    }

}
