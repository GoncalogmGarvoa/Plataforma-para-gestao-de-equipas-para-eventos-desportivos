package pt.arbitros.arbnet.repository.Auxss

import pt.arbitros.arbnet.domain.ConfirmationStatus
import pt.arbitros.arbnet.domain.Participant

interface ParticipantRepository {
    fun addParticipant(participant: Participant): Boolean

    fun findParticipantsByCallList(callListId: Int): List<Participant>

    fun updateConfirmationStatus(
        callListId: Int,
        matchDayId: Int,
        refereeId: Int,
        roleId: Int,
        status: ConfirmationStatus,
    ): Boolean

    fun removeParticipant(
        callListId: Int,
        matchDayId: Int,
        refereeId: Int,
        roleId: Int,
    ): Boolean
}
