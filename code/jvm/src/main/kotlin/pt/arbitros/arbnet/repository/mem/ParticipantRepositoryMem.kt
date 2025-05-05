package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.ConfirmationStatus
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.repository.ParticipantRepository

class ParticipantRepositoryMem : ParticipantRepository {
    private val participants = mutableListOf<Participant>()

//    private data class Participant(
//        val id: Int,
//        val callListId: Int,
//        val matchDayId: Int,
//        val councilId: Int,
//        val competitionIdMatchDay: Int,
//        val refereeId: Int,
//        val roleId: Int,
//        val confirmationStatus: String,
//    )

    override fun addParticipant(
        callListId: Int,
        matchDayId: Int,
        userId: Int,
        competitionId: Int,
        functionId: Int,
    ): Boolean {
        val participant =
            Participant(
                callListId = callListId,
                matchDayId = matchDayId,
                competitionIdMatchDay = competitionId,
                userId = userId,
                functionId = functionId,
                confirmationStatus = ConfirmationStatus.WAITING,
            )
        participants.add(participant)
        return true
    }

    override fun updateParticipantRole(
        participantId: Int,
        functionId: Int,
        matchDayId: Int,
    ): Boolean {
        val participant =
            participants.find {
                it.callListId == participantId && it.matchDayId == matchDayId
            }
        return if (participant != null) {
            participants.remove(participant)
            participants.add(participant.copy(functionId = functionId))
            true
        } else {
            false
        }
    }

    override fun updateParticipantConfirmationStatus(
        days: List<Int>,
        participantId: Int,
        callListId: Int,
    ): Boolean {
        var updated = false

        participants.forEachIndexed { index, p ->
            if (p.callListId == callListId && p.userId == participantId) {
                val newStatus = if (p.matchDayId in days) ConfirmationStatus.ACCEPTED else ConfirmationStatus.DECLINED
                if (p.confirmationStatus != newStatus) {
                    participants[index] = p.copy(confirmationStatus = newStatus)
                    updated = true
                }
            }
        }

        return updated
    }

    override fun getParticipantById(participantId: Int): pt.arbitros.arbnet.domain.Participant? {
        TODO("Not yet implemented")
    }

    override fun isCallListDone(callListId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun batchAddParticipants(participants: List<pt.arbitros.arbnet.domain.Participant>): Boolean {
        TODO("Not yet implemented")
    }
}
