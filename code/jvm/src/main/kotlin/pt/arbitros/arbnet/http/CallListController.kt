package pt.arbitros.arbnet.http

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.services.CallListService

@RestController
class CallListController(
    private val callListService: CallListService,
) {
    @PostMapping(Uris.CallListUris.CREATE_CALLLIST)
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
            callList.deadLine,
            callList.councilId,
            callList.participant,
            callList.matchDaySessions,
        )
}
//
// [
// {
//    "matchDay": 21,
//    "sessions": ["15:30"]
// },
// {
//    "matchDay": 22,
//    "sessions": ["09:00", "15:30"]
// },
// {
//    "matchDay": 23,
//    "sessions": ["09:00", "15:30"]
// }
// ]
