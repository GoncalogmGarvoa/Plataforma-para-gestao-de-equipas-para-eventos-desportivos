package pt.arbitros.arbnet.http.model

data class UserCreateTokenInputModel(
    val email: String,
    val password: String,
)
