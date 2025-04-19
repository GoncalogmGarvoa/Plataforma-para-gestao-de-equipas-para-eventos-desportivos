package pt.arbitros.arbnet.http.model

class UserUpdateInputModel(
    val name: String,
    val email: String,
    val birthDate: String,
    val iban: String,
    val password: String,
)
