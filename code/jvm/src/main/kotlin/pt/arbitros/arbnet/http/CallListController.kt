package pt.arbitros.arbnet.http

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.RoleAssignmentsInput
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
            callList.deadline,
            callList.councilId,
            callList.participant,
            callList.matchDaySessions,
        )

    @PostMapping(Uris.CallListUris.ASSIGN_ROLES)
    fun assingRoles(
        @RequestBody roleAssignmentsInfo: List<RoleAssignmentsInput>,
    ): Boolean =
        callListService.assignRoles(
            roleAssignmentsInfo,
        )
}
