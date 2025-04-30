package pt.arbitros.arbnet.http.model

class ParticipantUpdateInput(
    val days: List<Int>,
    val participantId: Int,
    val callListId: Int
)