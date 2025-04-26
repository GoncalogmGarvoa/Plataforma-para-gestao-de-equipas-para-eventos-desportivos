package pt.arbitros.arbnet.http.model

data class UserRolesUpdateInputModel (
    val userId: Int,
    val roles : Int,
    val matchDayId: Int,
)