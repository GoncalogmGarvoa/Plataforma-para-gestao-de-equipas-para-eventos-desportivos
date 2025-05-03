package pt.arbitros.arbnet.http.model

class UserUpdateInputModel(
    val id: Int,
    val name: String,
    val phoneNumber: String,
    val address: String,
    val email: String,
    val birthDate: String,
    val iban: String,
    val password: String,
)
