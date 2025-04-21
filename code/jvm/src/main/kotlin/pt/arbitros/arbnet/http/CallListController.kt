package pt.arbitros.arbnet.http

import org.springframework.web.bind.annotation.*
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.services.CallListService

@RestController
class CallListController(
    private val callListService: CallListService,
) {
    @PostMapping(Uris.CallList.CREATE_CALLLIST)
    fun createUser(
        @RequestBody callList: CallListInputModel,
    ): Int =
        callListService.createCallList(
            callList.competitionName,
            callList.competitionNumber,
            callList.address,
            callList.phoneNumber,
            callList.email,
            callList.association,
            callList.location,
            callList.participant,
            callList.timeLine,
            callList.type,
            callList.matchDays,
            callList.sessions,
        )
}
