package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.http.model.matchDayConfirmation

interface ParticipantRepository {
    fun addParticipant(
        callListId: Int,
        matchDayId: Int,
        userId: Int,
        functionId: Int,
        competitionId: Int,
    ): Boolean

    fun updateParticipantRole(
        participantId: Int,
        functionId: Int,
        matchDayId: Int,
    ): Boolean

    fun updateParticipantConfirmationStatus(
        days: List<matchDayConfirmation>,
        participantId: Int,
        callListId: Int,
    ): Boolean

    fun getParticipantsByCallList(callListId: Int): List<Participant>

    fun getParticipantById(participantId: Int): Participant?

    fun isCallListDone(callListId: Int): Boolean

    fun batchAddParticipants(participants: List<Participant>): Boolean

    fun deleteParticipantsByCallListId(callListId: Int): Boolean

}
