package pt.arbitros.arbnet.http.model

class UsersParametersOutputModel (
    val userId: Int,
    val userName: String,
    val userRoles: List<String>,
    val status: String
)