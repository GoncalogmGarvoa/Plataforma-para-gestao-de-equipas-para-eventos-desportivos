package pt.arbitros.arbnet.http.model

interface CallListInputLike {
    val competitionName: String
    val address: String
    val phoneNumber: String
    val email: String
    val association: String
    val location: String
    val callListType: String
    val userId: Int?
    val participants: List<ParticipantChoice>?
}