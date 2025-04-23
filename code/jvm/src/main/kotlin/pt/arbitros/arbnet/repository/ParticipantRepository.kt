package pt.arbitros.arbnet.repository

interface ParticipantRepository {
    fun addParticipant(
        callListId: Int,
        matchDayId: Int,
        councilId: Int,
        competitionId: Int,
        refereeId: Int,
        roleId: Int,
    ): Boolean

//    fun findParticipantsByCallList(callListId: Int): List<Participant>
//
//    fun updateConfirmationStatus(
//        callListId: Int,
//        matchDayId: Int,
//        refereeId: Int,
//        roleId: Int,
//        status: ConfirmationStatus,
//    ): Boolean
//
//    fun removeParticipant(
//        callListId: Int,
//        matchDayId: Int,
//        refereeId: Int,
//        roleId: Int,
//    ): Boolean
}
