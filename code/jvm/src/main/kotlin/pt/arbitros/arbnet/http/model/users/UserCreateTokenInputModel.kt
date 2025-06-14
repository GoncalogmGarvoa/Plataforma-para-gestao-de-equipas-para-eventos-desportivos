package pt.arbitros.arbnet.http.model.users

data class UserCreateTokenInputModel(
    val email: String,
    val password: String,
)