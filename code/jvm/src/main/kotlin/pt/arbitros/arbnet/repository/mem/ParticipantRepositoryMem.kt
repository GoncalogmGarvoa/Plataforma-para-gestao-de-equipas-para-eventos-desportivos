package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.repository.ParticipantRepository

class ParticipantRepositoryMem : ParticipantRepository {
    private val participants = mutableListOf<Participant>()

    private var nextId = 1

    private data class Participant(
        val id: Int,
        val callListId: Int,
        val matchDayId: Int,
        val councilId: Int,
        val competitionIdMatchDay: Int,
        val refereeId: Int,
        val roleId: Int,
        val confirmationStatus: String,
    )

    override fun addParticipant(
        callListId: Int,
        matchDayId: Int,
        councilId: Int,
        competitionId: Int,
        refereeId: Int,
        roleId: Int,
    ): Boolean {
        val participant =
            Participant(
                id = nextId++,
                callListId = callListId,
                matchDayId = matchDayId,
                councilId = councilId,
                competitionIdMatchDay = competitionId,
                refereeId = refereeId,
                roleId = roleId,
                confirmationStatus = "waiting",
            )
        participants.add(participant)
        return true
    }

    override fun updateParticipantRole(
        participantId: Int,
        roleId: Int,
        matchDayId: Int,
    ): Boolean {
        val participant =
            participants.find {
                it.callListId == participantId && it.matchDayId == matchDayId
            }
        return if (participant != null) {
            participants.remove(participant)
            participants.add(participant.copy(roleId = roleId))
            true
        } else {
            false
        }
    }

    override fun updateParticipantConfirmationStatus(
        days: List<Int>,
        participantId: Int,
        callListId: Int
    ): Boolean {
        var updated = false

        participants.forEachIndexed { index, p ->
            if (p.callListId == callListId && p.refereeId == participantId) {
                val newStatus = if (p.matchDayId in days) "accepted" else "declined"
                if (p.confirmationStatus != newStatus) {
                    participants[index] = p.copy(confirmationStatus = newStatus)
                    updated = true
                }
            }
        }

        return updated
    }

}
