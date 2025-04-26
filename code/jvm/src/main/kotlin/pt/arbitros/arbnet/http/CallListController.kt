package pt.arbitros.arbnet.http

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.http.model.CallListInputModel
import pt.arbitros.arbnet.http.model.ParticipantUpdateInput
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

    @PutMapping(Uris.CallListUris.UPDATE_PARTICIPANT_CONFIRMATION_STATUS)
    fun updateParticipantConfirmationStatus(
        @RequestBody participantUpdate: ParticipantUpdateInput
    )  : Boolean =
        callListService.updateParticipantConfirmationStatus(
            participantUpdate.days,
            participantUpdate.participantId,
            participantUpdate.callListId
        )
}
